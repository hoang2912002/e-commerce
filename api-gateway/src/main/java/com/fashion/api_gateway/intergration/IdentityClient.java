package com.fashion.api_gateway.intergration;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import com.fashion.api_gateway.common.response.ApiResponse;
import com.fashion.api_gateway.dto.request.VerifyTokenRequest;
import com.fashion.api_gateway.dto.response.VerifyTokenResponse;

import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/identity/auth/verifyAccessToken", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<VerifyTokenResponse>> verifyToken(@RequestBody VerifyTokenRequest request);
}
