package com.fashion.identity.service;

import java.util.List;
import java.util.UUID;

import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.entity.User;

public interface UserService {
    User findRawUserById(Object id);
    User lockUserById(Object id);
    User handleGetUserByUserName(String userName);
    User updateRefreshTokenUserByUserName(String userName, String refreshToken);
    UserResponse createUser(User user);
    UserResponse updateUser(User user);
    UserResponse getUserById(UUID id);
    PaginationResponse<List<UserResponse>> getAllUsers(UserSearchRequest request);
    void deleteUserById(UUID id);
}
