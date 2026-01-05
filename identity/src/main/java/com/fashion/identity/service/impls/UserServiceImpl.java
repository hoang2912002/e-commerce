package com.fashion.identity.service.impls;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.mapper.UserMapper;
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
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public User handleGetUserByUserName(String userName) {
        try {
            // Connect File service get avatar URL

            // Return user
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

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public User updateRefreshTokenUserByUserName(String userName, String refreshToken) {
        try {
            User user = this.userRepository.findByUserName(userName).orElseThrow(
                () -> new ServiceException(
                    EnumError.IDENTITY_USER_ERR_NOT_FOUND_USERNAME,
                    "user.not.found.userName",
                    Map.of("userName", userName)
                )
            );
            user.setRefreshToken(refreshToken);
            return this.userRepository.save(user);
        } catch (ServiceException e){
            throw e;
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: updateRefreshTokenUserByUserName(): {}", e.getMessage());
            throw e;
        }
        
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        try {
            return this.userRepository.findAll().stream().map(this.userMapper::toDtoNotRelationship).toList();
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: getAllUsers(): {}", e.getMessage());
            throw e;
        }
    }
    

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public UserResponse createUser(User user){
        try {
            log.info("IDENTITY-SERVICE: create new user");
            Optional<User> existedUser = this.userRepository.findByUserNameOrEmailOrPhoneNumber(user.getUserName(), user.getEmail(), user.getPhoneNumber());
            if(existedUser.isPresent()){
                if(existedUser.get().getPhoneNumber().equals(user.getPhoneNumber())){
                    throw new ServiceException(EnumError.IDENTITY_USER_DATA_EXISTED_PHONE_NUMBER,"user.exist.phoneNumber", Map.of("email", user.getEmail()));
                }
                else if(existedUser.get().getEmail().equals(user.getEmail())){
                    throw new ServiceException(EnumError.IDENTITY_USER_DATA_EXISTED_EMAIL,"user.exist.email", Map.of("email", user.getPhoneNumber()));
                }
            }
            return null;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: createUser: {}", e.getMessage());
            throw e;
        }
    }
}
