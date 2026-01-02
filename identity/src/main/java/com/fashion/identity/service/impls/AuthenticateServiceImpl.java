package com.fashion.identity.service.impls;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.dto.response.LoginResponse;
import com.fashion.identity.dto.response.LoginResponse.LoginResponseUserData;
import com.fashion.identity.dto.response.LoginResponse.UserInsideToken;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.service.AuthenticateService;
import com.fashion.identity.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticateServiceImpl implements AuthenticateService{
    final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    final JwtEncoder jwtEncoder;

    @Value("${jwt.access-token-in-seconds}")
    Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-in-seconds}")
    Long refreshTokenExpiration;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public String createAccessToken(String userName, LoginResponseUserData responseUserData) {
        try {
            return this.generateToken(userName, accessTokenExpiration, responseUserData);
        } catch(ServiceException e){
            throw e;
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: createAccessToken(): {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public String createRefreshToken(String userName, LoginResponseUserData responseUserData) {
        try {
            return this.generateToken(userName, refreshTokenExpiration, responseUserData);
        } catch(ServiceException e){
            throw e;
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: createRefreshToken(): {}", e.getMessage());
            throw e;
        }
    }

    private String generateToken(String userName, Long expiration,LoginResponseUserData responseUserData){
        try {
            final UserInsideToken userToken = UserInsideToken.builder()
                .id(responseUserData.getId())
                .fullName(responseUserData.getFullName())
                .userName(responseUserData.getUserName())
                .build();
            final Instant now = Instant.now();
            final Instant validity = now.plus(expiration, ChronoUnit.SECONDS);
            final JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                    .issuedAt(now)
                    .expiresAt(validity)
                    .subject(userName)
                    .claim("user", userToken);
            JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
            return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
        } catch (Exception e) {
            log.error("[generateToken] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
}
