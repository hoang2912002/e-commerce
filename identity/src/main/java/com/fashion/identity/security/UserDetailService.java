package com.fashion.identity.security;

import java.rmi.ServerException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component("userDetailsService")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailService implements UserDetailsService{
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username){
        try {
            Optional<User> userOptional = this.userRepository.findByUserName(username);
            if(!userOptional.isPresent()){
                throw new ServiceException(EnumError.IDENTITY_USER_ERR_NOT_FOUND_USERNAME_PASSWORD,"auth.login.error");
            }
            Role role = userOptional.get().getRole();
            String roleName = "";
            if(role instanceof Role &&
                Objects.nonNull(role.getId())
            ){
                roleName = role.getSlug().toUpperCase();
            }
            boolean enabled = userOptional.get().isEmailVerified();
            return new org.springframework.security.core.userdetails.User(
                userOptional.get().getUserName(), 
                userOptional.get().getPassword(), 
                enabled,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName))
            );
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: loadUserByUsername(): {}", e.getMessage());
            throw e;
        } 
    }
    
}
