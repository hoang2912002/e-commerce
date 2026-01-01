package com.fashion.identity.service;

import com.fashion.identity.entity.User;

public interface UserService {
    User handleGetUserByUserName(String userName);
}
