package com.fashion.identity.service.impls;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateServiceImpl implements AuthenticateService{
    UserService userService;
    
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public String createAccessToken(String userName) {
        try {
            User user = this.userService.handleGetUserByUserName(userName);
            return null;
        } catch(ServiceException e){
            throw e;
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: handleGetUserByUserName(): {}", e.getMessage());
            throw e;
        }
    }
    
}
