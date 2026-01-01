package com.fashion.identity.service.impls;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.repository.UserRepository;
import com.fashion.identity.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService{
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User handleGetUserByUserName(String userName) {
        try {
            return this.userRepository.findByUserName(userName).orElseThrow(
                () -> new ServiceException(
                    EnumError.IDENTITY_USER_ERR_NOT_FOUND_USERNAME,
                    "user.not.found.userName",
                    Map.of("userName", userName)
                )
            );
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: handleGetUserByUserName(): {}", e.getMessage());
            throw e;
        }
    }
    
}
