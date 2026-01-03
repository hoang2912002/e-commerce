package com.fashion.api_gateway.service.impls;

import org.springframework.stereotype.Service;

import com.fashion.api_gateway.common.response.ApiResponse;
import com.fashion.api_gateway.dto.request.VerifyTokenRequest;
import com.fashion.api_gateway.dto.response.VerifyTokenResponse;
import com.fashion.api_gateway.intergration.IdentityClient;
import com.fashion.api_gateway.service.IdentityService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityServiceImpl implements IdentityService {
    IdentityClient identityClient;

    public Mono<ApiResponse<VerifyTokenResponse>> verifyToken(String token){
        try {
            return identityClient.verifyToken(VerifyTokenRequest.builder().token(token).build()); 
        } catch (Exception e) {
            log.error("API-GATEWAY: verifyToken: {}", e.getMessage());
            throw e;
        }
    }
}
