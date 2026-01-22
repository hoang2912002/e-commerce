package com.fashion.order.config;

import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

public class AuthenticationRequestInterceptor implements RequestInterceptor{
    private static final List<String> FORWARDED_HEADERS = List.of(
            "Authorization", 
            "Accept-Language"
            // "X-Correlation-ID" // Thường dùng để tracing log
    );
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            for (String headerName : FORWARDED_HEADERS) {
                String headerValue = request.getHeader(headerName);
                if (StringUtils.hasText(headerValue)) {
                    template.header(headerName, headerValue);
                }
            }
        }
    }
    
}
