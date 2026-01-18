package com.fashion.inventory.common.response;

import java.util.Locale;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fashion.inventory.common.annotation.ApiMessageResponse;
import com.fashion.inventory.common.util.MessageUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestControllerAdvice
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ResponseWrapper implements ResponseBodyAdvice<Object>{
    MessageUtil messageUtil;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
    
    @Override
    public @Nullable Object beforeBodyWrite(
        @Nullable Object body, 
        MethodParameter returnType, 
        MediaType selectedContentType,
        Class selectedConverterType, 
        ServerHttpRequest request, 
        ServerHttpResponse response
    ) {
        if(body == null || body instanceof ApiResponse || body instanceof String){
            return body;
        }

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse(); 
        int code = servletResponse.getStatus();

        if(code >= 400){
            return body;
        }

        ApiMessageResponse messageResponse = returnType.getMethodAnnotation(ApiMessageResponse.class);
        String message = messageResponse != null ? messageResponse.value() : "server.call.api.success";

        String path = request.getURI().getPath();

        Locale locale = request.getHeaders()
            .getAcceptLanguageAsLocales()
            .stream()
            .findFirst()
            .orElse(Locale.ENGLISH);

        LocaleContext localeContext = () -> locale;

        String messageSuccess = Objects.requireNonNullElse(
            messageUtil.getMessage(message, localeContext),
            "CALL API SUCCESS"
        );

        return ApiResponse.success(messageSuccess,body, path);
    }

    
}
