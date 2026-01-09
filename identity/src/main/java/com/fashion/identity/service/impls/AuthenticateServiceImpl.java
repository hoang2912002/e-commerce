package com.fashion.identity.service.impls;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.common.util.FormatTime;
import com.fashion.identity.dto.request.VerifyEmailRequest;
import com.fashion.identity.dto.response.LoginResponse;
import com.fashion.identity.dto.response.LoginResponse.LoginResponseUserData;
import com.fashion.identity.dto.response.LoginResponse.UserInsideToken;
import com.fashion.identity.dto.response.kafka.UserRegisterEvent;
import com.fashion.identity.dto.response.VerifyEmailResponse;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.mapper.RoleMapper;
import com.fashion.identity.messaging.producer.IdentityServiceProducer;
import com.fashion.identity.repository.UserRepository;
import com.fashion.identity.security.SecurityUtils;
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
    final SecurityUtils securityUtils;
    final UserRepository userRepository;
    final RoleMapper roleMapper;
    final IdentityServiceProducer identityServiceProducer;

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
            log.error("IDENTITY-SERVICE: createAccessToken(): {}", e.getMessage());
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
            log.error("IDENTITY-SERVICE: createRefreshToken(): {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean verifyAccessToken(String token){
        try {
            Jwt jwt = securityUtils.getUserFromJWTToken(token);
            return Objects.nonNull(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest verifyEmailRequest){
        try {
            User user = this.userRepository.lockUserByEmail(verifyEmailRequest.getEmail());
            if(Objects.isNull(user))
                throw new ServiceException(EnumError.IDENTITY_USER_ERR_NOT_FOUND_EMAIL,"user.not.found.email", Map.of("email", verifyEmailRequest.getEmail()));
            if(!verifyEmailRequest.getVerifyCode().endsWith(user.getVerificationCode()))
                throw new ServiceException(EnumError.IDENTITY_USER_INVALID_VERIFY_CODE,"user.verifyCode.invalid");
            if(user.getVerificationExpiration().isBefore(LocalDateTime.now()))
                throw new ServiceException(EnumError.IDENTITY_USER_INVALID_VERIFY_EXPIRATION,"user.verifyExpiration.invalid");
            
            LoginResponseUserData userData = LoginResponseUserData.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .userName(user.getUserName())
                .avatar(null)
                .role(roleMapper.toDto(user.getRole()))
                .build();
            
            String accessToken = this.generateToken(user.getUserName(), accessTokenExpiration, userData);
            String refreshToken = this.generateToken(user.getUserName(), refreshTokenExpiration, userData);

            user.setActivated(true);
            user.setEmailVerified(true);
            user.setRefreshToken(refreshToken);
            user.setVerificationCode(null);
            this.userRepository.saveAndFlush(user);

            // Send mail register successful
            this.identityServiceProducer.produceUserVerifyEventSuccess(
                UserRegisterEvent.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .verificationAt(FormatTime.StringDateLocalDateTime(LocalDateTime.now()))
                .build()
            );

            LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .user(userData)
                .build();
                
            return VerifyEmailResponse.builder().response(loginResponse).refreshToken(refreshToken).build();
        } catch(ServiceException e){
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [verifyEmail] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
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
            log.error("IDENTITY-SERVICE: generateToken Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
}
