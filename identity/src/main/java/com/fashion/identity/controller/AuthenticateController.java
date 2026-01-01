package com.fashion.identity.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.identity.common.annotation.ApiMessageResponse;
import com.fashion.identity.dto.request.LoginRequest;
import com.fashion.identity.dto.response.LoginResponse;
import com.fashion.identity.service.AuthenticateService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticateController {
    AuthenticateService authenticateService;

    @PostMapping("/login")
    @ApiMessageResponse("auth.success.login")
    public ResponseEntity<LoginResponse> login(
        @RequestBody @Valid LoginRequest request
    ) {
        this.authenticateService.createAccessToken(request.getUserName());
        return ResponseEntity.ok(
            LoginResponse.builder()
                .accessToken("xxx")
                .build()
        );
    }
    
}
