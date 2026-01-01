package com.fashion.identity.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.common.response.ApiResponse;
import com.fashion.identity.common.util.MessageUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GlobalException {
    MessageUtil messageUtil;

    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<ApiResponse> handleServiceException(ServiceException ex, HttpServletRequest request) {

        String languageHeader = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        Locale locale = request.getLocale(); // ⭐ RECOMMENDED
        LocaleContext localeContext = () -> locale;
        String path = request.getRequestURI();
        // Lấy EnumError từ exception
        EnumError enumError = ex.getEnumError();
        Object args = ex.getErrors() != null ? ex.getErrors().values().toString() : new Object[]{};
        ApiResponse<Object> res = ApiResponse.builder()
            .success(false)
            .code(enumError.getHttpStatus().value())
            .message(messageUtil.getMessage(ex.getMessageCode(), localeContext, args))
            .language(languageHeader)
            .errorCode(ex.getMessageCode())
            .timestamp(LocalDateTime.now())
            .errors(ex.getErrors())
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
                .language(languageHeader)
                .errorCode(messageCode)
                .errors(Map.of(fieldName, message))
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
