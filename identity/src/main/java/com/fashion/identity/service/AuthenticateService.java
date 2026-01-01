package com.fashion.identity.service;

import com.fashion.identity.dto.response.LoginResponse;

public interface AuthenticateService {
    String createAccessToken(String email);
}
