package com.fashion.order.security;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.fashion.order.common.enums.EnumError;
import com.fashion.order.dto.response.internal.UserResponse.UserInsideToken;
import com.fashion.order.exception.ServiceException;
import com.nimbusds.jose.util.Base64;

@Component
public class SecurityUtils {
    @Value("${jwt.base64-secret}")
    private String jwtKey;

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .filter(authentication -> authentication.getCredentials() instanceof String)
            .map(authentication -> (String) authentication.getCredentials());
    }

    public static Optional<UserInsideToken> getCurrentUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .filter(authentication -> authentication.getPrincipal() instanceof ClaimAccessor)
            .map(authentication -> (ClaimAccessor) authentication.getPrincipal())
            .map(principal -> {
                Map<String, Object> userMap = principal.getClaim("user");
                if (userMap == null) return null;

                UserInsideToken user = new UserInsideToken();
                user.setId(UUID.fromString(userMap.get("id").toString()));
                user.setFullName(userMap.get("fullName").toString());
                user.setUserName(userMap.get("userName").toString());
                return user;
            });
    }

    public Jwt getUserFromJWTToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            // throw e;
            throw new ServiceException(EnumError.INVENTORY_USER_INVALID_REFRESH_TOKEN, "user.refresh.token.notFormat", Map.of("refresh_token", "wrong"));
        }
    }

    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
