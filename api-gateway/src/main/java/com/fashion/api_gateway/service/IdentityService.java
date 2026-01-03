package com.fashion.api_gateway.service;

import com.fashion.api_gateway.common.response.ApiResponse;
import com.fashion.api_gateway.dto.response.VerifyTokenResponse;

import reactor.core.publisher.Mono;

public interface IdentityService {
    Mono<ApiResponse<VerifyTokenResponse>> verifyToken(String token);
}
