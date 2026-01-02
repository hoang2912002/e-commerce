package com.fashion.identity.service;

import java.util.List;

import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.entity.User;

public interface UserService {
    User handleGetUserByUserName(String userName);
    User updateRefreshTokenUserByUserName(String userName, String refreshToken);
    List<UserResponse> getAllUsers();
}
