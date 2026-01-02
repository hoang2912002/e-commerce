package com.fashion.identity.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.identity.common.annotation.ApiMessageResponse;
import com.fashion.identity.dto.request.LoginRequest;
import com.fashion.identity.dto.response.LoginResponse;
import com.fashion.identity.dto.response.LoginResponse.LoginResponseUserData;
import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;
import com.fashion.identity.mapper.RoleMapper;
import com.fashion.identity.service.AuthenticateService;
import com.fashion.identity.service.UserService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Objects;

import org.apache.kafka.common.protocol.types.Field.Str;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticateController {
    final AuthenticateService authenticateService;
    final UserService userService;
    final AuthenticationManagerBuilder authenticationManagerBuilder;
    final RoleMapper roleMapper;

    @Value("${cookie-settings.path}")
    private String cookiePath;
    
    @Value("${cookie-settings.sameSite}")
    private String cookieSameSite;
    
    @Value("${cookie-settings.domain}")
    private String cookieDomain;

    @Value("${jwt.refresh-token-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    @ApiMessageResponse("auth.success.login")
    public ResponseEntity<LoginResponse> login(
        @RequestBody @Valid LoginRequest request
    ) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            request.getUserName(), 
            request.getPassword()
        );
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUserLogin = this.userService.handleGetUserByUserName(request.getUserName());
        LoginResponse responseLoginDTO = new LoginResponse();
        LoginResponseUserData userDTO = null;
        String refresh_token = "";
        if (Objects.nonNull(currentUserLogin)) {
            userDTO = LoginResponseUserData.builder()
            .id(currentUserLogin.getId())
            .fullName(currentUserLogin.getFullName())
            .email(currentUserLogin.getEmail())
            .userName(currentUserLogin.getUserName())
            .phoneNumber(currentUserLogin.getPhoneNumber())
            .avatar(null)
            .role(roleMapper.toDto(currentUserLogin.getRole()))
            .build();

            // Render refresh token and update to DB
            refresh_token = this.authenticateService.createRefreshToken(request.getUserName(), userDTO);
            this.userService.updateRefreshTokenUserByUserName(request.getUserName(), refresh_token);
        }
        // Render access token
        responseLoginDTO.setUser(userDTO);
        String accessToken = this.authenticateService.createAccessToken(request.getUserName(), responseLoginDTO.getUser());
        responseLoginDTO.setAccessToken(accessToken);

        //Create cookie for refresh_token
        ResponseCookie springCookie = ResponseCookie.from("refresh_token", refresh_token)
            .httpOnly(true)
            .secure(true)
            .path(cookiePath)
            .maxAge(refreshTokenExpiration)
            .sameSite(cookieSameSite)
            .domain(cookieDomain)
            .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
            .body(responseLoginDTO);
    }
    
}
