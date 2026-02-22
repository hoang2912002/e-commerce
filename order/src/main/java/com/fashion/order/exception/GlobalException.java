package com.fashion.order.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fashion.order.common.enums.EnumError;
import com.fashion.order.common.response.ApiResponse;
import com.fashion.order.common.util.MessageUtil;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GlobalException {
    final MessageUtil messageUtil;

    @Value("${spring.application.language-default}")
    String defaultLanguage;

    // Xử lý lỗi Business exception
    @ExceptionHandler(value = {
        ServiceException.class,
        KafkaException.class
    })
    public ResponseEntity<ApiResponse> handleServiceException(RuntimeException ex, HttpServletRequest request) {
        EnumError enumError = null;
        String errorCode = null;
        Map<String, Object> errors = new HashMap<>();
        Object args = new Object[]{};
        
        String languageHeader = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        Locale locale = request.getLocale();
        LocaleContext localeContext = () -> locale;
        String path = request.getRequestURI();

        if(ex instanceof ServiceException sEx){
            enumError = sEx.getEnumError();
            errorCode = sEx.getMessageCode();
            args = sEx.getErrors() != null ? sEx.getErrors().values().toString() : new Object[]{};
            errors = sEx.getErrors();
        } else if(ex instanceof KafkaException kEx){
            enumError = kEx.getEnumError();
            errorCode = kEx.getMessageCode();
            args = kEx.getErrors() != null ? kEx.getErrors().values().toString() : new Object[]{};
            errors = kEx.getErrors();
        } else {
            enumError = EnumError.ORDER_INTERNAL_ERROR_CALL_API;
            errorCode = "server.error.internal";
            errors = null;
        }

        String message = messageUtil.getMessage(errorCode, localeContext, args);
        // Lấy EnumError từ exception
        ApiResponse<Object> res = ApiResponse.builder()
            .success(false)
            .code(enumError.getHttpStatus().value())
            .message(message)
            .language(Objects.nonNull(languageHeader) ? languageHeader : defaultLanguage)
            .errorCode(errorCode)
            .timestamp(LocalDateTime.now())
            .errors(errors)
            .path(path)
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String languageHeader = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        Locale locale = request.getLocale(); // ⭐ RECOMMENDED
        LocaleContext localeContext = () -> locale;

        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        String path = request.getRequestURI();

        FieldError fieldError = fieldErrors.getFirst();

        String messageCode = fieldError.getDefaultMessage(); // ví dụ: "user.email.notnull"
        String fieldName = fieldError.getField();

        String message = messageUtil.getMessage(messageCode, localeContext);

        ApiResponse<Object> res = ApiResponse.<Object>builder()
                .success(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .language(Objects.nonNull(languageHeader) ? languageHeader : defaultLanguage)
                .errorCode(EnumError.ORDER_INTERNAL_ERROR_CALL_API.getCode())
                .errors(Map.of(fieldName, message))
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
    
    /**
     * 
     * @param RequestNotPermitted.class => rate limiter
     * @param CallNotPermittedException.class => circuit breaker
     * 
     */
    @ExceptionHandler(value = {
        CallNotPermittedException.class,
        RequestNotPermitted.class,
    })
    public ResponseEntity<ApiResponse<Object>> handleCallNotPermitted(RuntimeException ex, HttpServletRequest request) {

        String errorCode = null;
        Map<String, Object> errors = new HashMap<>();
        
        String languageHeader = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        Locale locale = request.getLocale();
        LocaleContext localeContext = () -> locale;
        String path = request.getRequestURI();
        String message = "resilience4j.combined";

        if (ex instanceof CallNotPermittedException cEx){
            errorCode = EnumError.IDENTITY_RESILIENCE4J_CIRCUIT_BREAKER_OPEN.getCode();
            message = messageUtil.getMessage("resilience4j.circuitBreaker.open", localeContext);
            errors = Map.of("circuitBreaker", message);
        } else if(ex instanceof RequestNotPermitted rEx){
            errorCode = EnumError.IDENTITY_RESILIENCE4J_RATE_LIMITER.getCode();
            message = messageUtil.getMessage("resilience4j.rateLimiter", localeContext);
            errors = Map.of("rateLimiter", message);
        }

        ApiResponse<Object> res = ApiResponse.builder()
                .success(false)
                .code(HttpStatus.UNAUTHORIZED.value())
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .language(Objects.nonNull(languageHeader) ? languageHeader : defaultLanguage)
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(res);
    }
}
