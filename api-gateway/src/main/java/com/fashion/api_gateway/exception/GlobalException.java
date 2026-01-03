package com.fashion.api_gateway.exception;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import com.fashion.api_gateway.common.enums.EnumError;
import com.fashion.api_gateway.common.response.ApiResponse;
import com.fashion.api_gateway.common.util.MessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@Component
@Order(-2)

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
/**
 * 1️⃣ Class này THỰC CHẤT LÀ GÌ?
 *      ErrorWebExceptionHandler nó không phải là filter chain nó là 1 exception chung cho nếu có bất kỳ lỗi nào xảy ra 
 *      thì sẽ chạy vô thằng này mặc định là DefaultErrorWebExceptionHandler
 * 2️⃣ Kiến trúc tầng (từ ngoài vào trong)
 *      ┌────────────────────────────────────┐
        │  Netty / Reactor HTTP Server       │
        └────────────────────────────────────┘
                        ↓
        ┌────────────────────────────────────┐
        │  WebFilter Chain (WebFlux)         │
        │  ├─ CORS WebFilter                 │
        │  ├─ Security WebFilter (Auth)      │
        │  ├─ Gateway GlobalFilter           │
        │  └─ Custom WebFilter               │
        └────────────────────────────────────┘
                        ↓
        ┌────────────────────────────────────┐
        │  Handler / Routing                 │
        │  ├─ Gateway Route Predicate        │
        │  ├─ Route Filter                   │
        │  └─ Controller / Forwarding        │
        └────────────────────────────────────┘
                        ↓
        ❌ Exception xảy ra
                        ↓
        ┌────────────────────────────────────┐
        │  Error Handling Layer              │
        │  ├─ ErrorWebExceptionHandler       │
        │  └─ DefaultErrorWebExceptionHandler│
        └────────────────────────────────────┘
       
        Tầng WebFilter Chain 
        | Filter               | Order |
        | -------------------- | ----- |
        | CORS                 | -100  |
        | Security             | 0     |
        | Gateway GlobalFilter | -1    |
        | Custom filter        | tuỳ   |

 * 3️⃣ VẬY @Order(-2) CÓ Ý NGHĨA GÌ?
 *      Spring có NHIỀU ErrorWebExceptionHandler
 *      | Handler                                         | Order |
        | ----------------------------------------------- | ----- |
        | DefaultErrorWebExceptionHandler                 | -1    |
        | Custom ErrorWebExceptionHandler (nếu không set) |  0    |
 *      Vì vậy muốn spring chạy vô hàm custom này của mình thì phải set Order nhỏ hơn -1 => -2
 *      
 * 4️⃣ VẬY CÓ MÂU THUẪN GÌ VỚI các layer khác KHÔNG?
 *      Không hề mâu thuẫn vì nó nằm ở layer của exception cho nên @Order chỉ có tác dụng trong layer Exception
 *      
 */
public class GlobalException implements ErrorWebExceptionHandler {
    MessageUtil messageUtil;
    ObjectMapper objectMapper;

    @Override
    /**
     * 0️⃣ Ngữ cảnh của method này
     *      Đây là ErrorWebExceptionHandler
     *      Chỉ được gọi khi exception đã thoát ra khỏi WebFilter / Handler / Gateway
     *      Là điểm chặn CUỐI CÙNG trước khi response trả về client
     */
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        /**
         * 1️⃣ Lấy response & kiểm tra đã commit chưa
         *      response.isCommitted() = header/body đã gửi về client
         *      Khi đã commit:
         *          Không sửa status
         *          Không ghi body được nữa
         *          👉 Nếu đã commit → ném lại exception cho Netty xử lý
         */
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        /**
         * 2️⃣ Set Content-Type = JSON
         *      Khai báo response trả về là JSON
         *      Vì mình tự ghi body, Spring không tự set hộ
         */
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        /**
         * 3️⃣ Khởi tạo status & errorCode mặc định
         *      Fallback an toàn
         *      Nếu exception lạ → vẫn trả được response chuẩn
         */
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "UNCATEGORIZED_ERROR";
        
        if (ex instanceof AppException) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = EnumError.API_GATEWAY_UNAUTHORIZED.getCode();
        }
        
        response.setStatusCode(status);

        /**
         * 4️⃣ Lấy thông tin ngôn ngữ (i18n)
         *      Accept-Language: client gửi lên
         *      LocaleContext: Spring resolve locale (header / default)
         */
        String languageHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT_LANGUAGE);
        Locale locale = exchange.getLocaleContext().getLocale();
        String message = messageUtil.getMessage("auth.token.invalid.login.again", () -> locale);

        // Build ApiResponse y hệt format cũ của bạn
        ApiResponse<Object> res = ApiResponse.builder()
                .success(false)
                .code(status.value())
                .errorCode(errorCode)
                .message(message)
                .path(exchange.getRequest().getURI().getPath())
                .errors(Map.of("accessToken", message))
                .timestamp(LocalDateTime.now())
                .language(languageHeader)
                .build();
        
        /**
         * 5️⃣ Ghi body theo reactive chuẩn
         *      WebFlux KHÔNG serialize tự động ở ErrorWebExceptionHandler
         *      Phải tự ghi DataBuffer
         *      
         * DataBufferFactory bufferFactory = response.bufferFactory();
         * return bufferFactory.wrap(data);
         *      Wrap thành DataBuffer => Netty chỉ nhận DataBuffer
         */
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                byte[] data = objectMapper.writeValueAsBytes(res);
                return bufferFactory.wrap(data);
            } catch (JsonProcessingException e) {
                return bufferFactory.wrap("".getBytes());
            }
        }));

        /**
         * 1. Luồng xử lý (Workflow)
         *      a. Phát sinh lỗi: Trong AuthenticationFilter, khi bạn gọi new AppException(...), request sẽ lập tức dừng lại và văng ra một Exception.
         *      b. Đánh chặn: Vì class GlobalException của bạn thực thi giao diện ErrorWebExceptionHandler và có @Order(-2), 
         *          Spring Cloud Gateway sẽ ưu tiên giao request bị lỗi này cho nó xử lý thay vì dùng cơ chế báo lỗi mặc định của Spring.
         *      c. Kiểm tra trạng thái: Lệnh response.isCommitted() kiểm tra xem Gateway đã bắt đầu gửi dữ liệu về cho Client chưa. Nếu đã gửi rồi thì không thể can thiệp sửa Header hay Body được nữa.
         *      d. Đóng gói Response: * Nó lấy thông tin từ Exception (ex) để quyết định mã lỗi (401 hay 500).
         *          Nó lấy Locale từ exchange để dịch thông báo lỗi sang ngôn ngữ tương ứng (i18n).
         *          Nó xây dựng đối tượng ApiResponse chuẩn mà bạn đã định nghĩa.
         *      e. Ghi dữ liệu (Serialization): Đây là bước quan trọng nhất. Vì Gateway chạy Non-blocking, 
         *      nó không trả về String hay JSON ngay mà trả về một DataBuffer. Dữ liệu được biến thành mảng Byte và đổ vào luồng (Stream) phản hồi.
         *     ┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
         *     │ Đặc điểm	    │   Spring MVC (Identity Service)	                        │ Spring WebFlux (Gateway)                                           │
               │ Server	        │   Tomcat	                                                │ Netty                                                              │
               │ Cơ chế	        │   Thread-per-request (Chặn)	                            │ Event Loop (Không chặn)                                            │
               │ Xử lý lỗi	    │   @RestControllerAdvice (Chỉ bắt lỗi trong Controller)	│ ErrorWebExceptionHandler (Bắt lỗi toàn bộ hệ thống kể cả Filter)   │   
               │ Dữ liệu trả về │   ResponseEntity<Object>	                                │ Mono<Void> (Viết trực tiếp vào Buffer)                             │
               └─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
         */
    }

}
