package com.fashion.identity.service;

import com.fashion.identity.dto.request.VerifyEmailRequest;
import com.fashion.identity.dto.response.LoginResponse;
import com.fashion.identity.dto.response.LoginResponse.LoginResponseUserData;
import com.fashion.identity.dto.response.VerifyEmailResponse;

public interface AuthenticateService {
    String createAccessToken(String email, LoginResponseUserData responseUserData);
    String createRefreshToken(String email, LoginResponseUserData responseUserData);
    boolean verifyAccessToken(String token);
    VerifyEmailResponse verifyEmail(VerifyEmailRequest verifyEmailRequest);
}
