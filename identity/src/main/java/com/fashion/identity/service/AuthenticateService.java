package com.fashion.identity.service;

import com.fashion.identity.dto.response.LoginResponse.LoginResponseUserData;

public interface AuthenticateService {
    String createAccessToken(String email, LoginResponseUserData responseUserData);
    String createRefreshToken(String email, LoginResponseUserData responseUserData);
    boolean verifyAccessToken(String token);
}
