package com.fashion.identity.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.identity.common.annotation.ApiMessageResponse;
import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.common.response.ApiResponse;
import com.fashion.identity.dto.request.LoginRequest;
import com.fashion.identity.dto.request.UserRequest;
import com.fashion.identity.dto.request.VerifyEmailRequest;
import com.fashion.identity.dto.request.VerifyTokenRequest;
import com.fashion.identity.dto.response.LoginResponse;
import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.dto.response.VerifyEmailResponse;
import com.fashion.identity.dto.response.VerifyTokenResponse;
import com.fashion.identity.dto.response.LoginResponse.LoginResponseUserData;
import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.mapper.RoleMapper;
import com.fashion.identity.mapper.UserMapper;
import com.fashion.identity.service.AuthenticateService;
import com.fashion.identity.service.UserService;
import com.nimbusds.jose.JOSEException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Objects;

import org.apache.kafka.common.protocol.types.Field.Str;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Retry(name = "identity-service", fallbackMethod = "resilience4jRetryFallback")
@CircuitBreaker(name = "identity-service", fallbackMethod = "resilience4jCircuitBreakerFallback")
@RateLimiter(name = "identity-service", fallbackMethod = "resilience4jRateLimiterFallback")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticateController extends R4jFallback {
    final AuthenticateService authenticateService;
    final UserService userService;
    final AuthenticationManagerBuilder authenticationManagerBuilder;
    final RoleMapper roleMapper;
    final UserMapper userMapper;

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
    
    @PostMapping("/verifyAccessToken")
    @ApiMessageResponse("auth.success.verify.access.token")
    ResponseEntity<VerifyTokenResponse> verifyAccessToken(@RequestBody VerifyTokenRequest request)
    {
        boolean result = authenticateService.verifyAccessToken(request.getToken());
        return ResponseEntity.ok(VerifyTokenResponse.builder().isValid(result).build());
    }

    @PostMapping("/register")
    @ApiMessageResponse("auth.success.register")
    public ResponseEntity<UserResponse> register(@RequestBody @Validated(UserRequest.Create.class) UserRequest entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUser(this.userMapper.toValidated(entity)));
    }
    
    @PostMapping("/verifyEmail")
    @ApiMessageResponse("auth.success.verify.email")
    public ResponseEntity<LoginResponse> verifyEmail(@RequestBody @Valid VerifyEmailRequest verifyEmailRequest) {
        VerifyEmailResponse verifyEmailResponse = this.authenticateService.verifyEmail(verifyEmailRequest);

        //Create cookie for refresh_token
        ResponseCookie springCookie = ResponseCookie.from("refresh_token", verifyEmailResponse.getRefreshToken())
            .httpOnly(true)
            .secure(true)
            .path(cookiePath)
            .maxAge(refreshTokenExpiration)
            .sameSite(cookieSameSite)
            .domain(cookieDomain)
            .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
            .body(verifyEmailResponse.getResponse());
    }
    
}
