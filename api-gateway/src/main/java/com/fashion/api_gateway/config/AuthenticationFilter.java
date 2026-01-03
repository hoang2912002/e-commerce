package com.fashion.api_gateway.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fashion.api_gateway.common.enums.EnumError;
import com.fashion.api_gateway.common.response.ApiResponse;
import com.fashion.api_gateway.exception.AppException;
import com.fashion.api_gateway.service.IdentityService;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    IdentityService identityService;
    com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // List endpoint no check authorization
    @NonFinal
    private String[] publicEndpoint = {
        "/identity/auth/.*",
        "/identity/users/registration",
        "/file/media/download/.*"
        // "/file/.*"
    };

    @NonFinal
    @Value("${app.api-prefix}")
    private String apiPrefix;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("--- Enter AuthenticationFilter ---");

        // If public endpoint next chain
        if(isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);
        

        // Get token
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if(CollectionUtils.isEmpty(authHeader)){
            // return unAuthenticated(exchange.getResponse());
            throw new AppException(EnumError.API_GATEWAY_UNAUTHORIZED);
        }

        String token = authHeader.getFirst().replace("Bearer ", "");

        return identityService.verifyToken(token).flatMap(
            res -> {
                if(res.getData().isValid())
                    return chain.filter(exchange);
                // return unAuthenticated(exchange.getResponse());
                throw new AppException(EnumError.API_GATEWAY_UNAUTHORIZED);
            }
        ).onErrorResume(
            throwable -> {
                // onErrorResume lỗi connect bắn error
                log.error(throwable.getMessage());
                // return unAuthenticated(exchange.getResponse());
                throw new AppException(EnumError.API_GATEWAY_UNAUTHORIZED);
            }
        );
       
    }

    private boolean isPublicEndpoint(ServerHttpRequest request){
        return Arrays.stream(publicEndpoint).anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

}
