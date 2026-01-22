package com.fashion.order.common.response;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    boolean success;
    Integer code;
    String message;
    T data;
    String path;
    Map<String, Object> errors;
    String errorCode;
    LocalDateTime timestamp;
    String language;

    public static <T> ApiResponse<T> success(T data, String path) {
        return success("Success", data, path);
    }

    public static <T> ApiResponse<T> success(String message, T data, String path) {
        Locale locale = LocaleContextHolder.getLocale();
        return ApiResponse.<T>builder()
            .success(true)
            .code(HttpStatus.OK.value())
            .message(message)
            .data(data)
            .path(path)
            .language(locale.getLanguage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String message, String path) {
        Locale locale = LocaleContextHolder.getLocale();
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),message,locale.getLanguage(), null, null, path);
    }

    public static <T> ApiResponse<T> error(int status, String message, String path) {
        Locale locale = LocaleContextHolder.getLocale();
        return error(status,message,locale.getLanguage(), null, null, path);
    }

    public static <T> ApiResponse<T> error(int status, String message, String language, String path) {
        return error(status,message,language, null, null, path);
    }

    // ✅ ERROR overload without errors
    public static <T> ApiResponse<T> error(int status, String message, String language, String errorCode, String path) {
        return error(status, message, language, errorCode, null, path);
    }

    public static <T> ApiResponse<T> error(int status, String message, String language, String errorCode, Map<String, Object> errors, String path) {
        return ApiResponse.<T>builder()
            .success(false)
            .code(status)
            .message(message)
            .errorCode(errorCode)
            .path(path)
            .errors(errors)
            .language(language)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
