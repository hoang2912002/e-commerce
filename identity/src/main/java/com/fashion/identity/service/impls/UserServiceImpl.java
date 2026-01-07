package com.fashion.identity.service.impls;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.common.util.ConvertUuidUtil;
import com.fashion.identity.dto.response.AddressResponse;
import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.identity.entity.Address;
import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.mapper.UserMapper;
import com.fashion.identity.repository.RoleRepository;
import com.fashion.identity.repository.UserRepository;
import com.fashion.identity.service.AddressService;
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
    AddressService addressService;
    RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public User findRawUserById(Object id) {
        return this.userRepository.findById(ConvertUuidUtil.toUuid(id))
                .orElseThrow(() -> new ServiceException(EnumError.IDENTITY_ROLE_ERR_NOT_FOUND_ID, "user.not.found.id",Map.of("id", id)));
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public User lockUserById(Object id){
        try {
            return this.userRepository.lockUserById(ConvertUuidUtil.toUuid(id));
        } catch (Exception e) {
            log.error("[lockProductById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

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
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
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
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
        
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        try {
            return this.userRepository.findAll().stream().map(this.userMapper::toDtoNotRelationship).toList();
        } catch (Exception e) {
            log.error("INTERNAL-SERVICE: getAllUsers(): {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public UserResponse createUser(User user){
        log.info("IDENTITY-SERVICE: create new user");
        try {
            checkExistUser(user.getUserName(), user.getEmail(), user.getPhoneNumber(), null);
            
            Role role = Objects.nonNull(user.getRole()) && Objects.nonNull(user.getRole().getId()) ? 
                this.roleRepository.findById(user.getRole().getId()).get() :
                this.roleRepository.findBySlug(RoleServiceImpl.roleUser);

            if(Objects.isNull(role)){
                throw new ServiceException(EnumError.IDENTITY_ROLE_ERR_NOT_FOUND_ID,"role.not.found.id", Map.of("roleId", user.getRole().getId()));
            }
            final User userForCreate = User
                .builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(this.passwordEncoder.encode(user.getPassword()))
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .userName(user.getUserName())
                .dob(user.getDob())
                .role(role)
                .build();
            final User savedUser = this.userRepository.save(userForCreate);
            // Save address
            final List<InnerAddressResponse> addressServices = this.addressService.handleAddressesForUser(
                savedUser, 
                user.getAddresses(), 
                addr -> InnerAddressResponse.builder()
                    .id(addr.getId())
                    .address(addr.getAddress())
                    .province(addr.getProvince())
                    .district(addr.getDistrict())
                    .ward(addr.getWard())
                    .build()
            );
            final UserResponse userDTO = userMapper.toDto(savedUser);
            userDTO.setAddresses(addressServices);
            return userDTO;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: createUser: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public UserResponse getUserById(String id){
        try {
            final User user = this.findRawUserById(id);
            if(Objects.isNull(user)){
                throw new ServiceException(EnumError.IDENTITY_USER_ERR_NOT_FOUND_ID, "user.not.found.id",Map.of("id", id));
            }
            return userMapper.toDto(user);
        } catch (Exception e) {
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public UserResponse updateUser( User user) {
        log.info("IDENTITY-SERVICE: updateUser update an user");
        try {
            // Lock row
            final User updateUser = this.lockUserById(user.getId());
            
            // Check another user
            checkExistUser(user.getUserName(), user.getEmail(), user.getPhoneNumber(), user.getId());
            
            updateUser.setDob(user.getDob());
            updateUser.setGender(user.getGender());

            updateUser.setFullName(user.getFullName());

            Role role = Objects.nonNull(user.getRole()) && Objects.nonNull(user.getRole().getId()) ? 
                this.roleRepository.findById(user.getRole().getId()).get() :
                this.roleRepository.findBySlug(RoleServiceImpl.roleUser);
            
            if(Objects.isNull(role)){
                throw new ServiceException(EnumError.IDENTITY_ROLE_ERR_NOT_FOUND_ID,"role.not.found.id", Map.of("roleId", user.getRole().getId()));
            }

            updateUser.setRole(role);

            final List<AddressResponse.InnerAddressResponse> addresses = 
                this.addressService.handleAddressesForUser(
                    updateUser, user.getAddresses(),
                    addr -> new AddressResponse.InnerAddressResponse(
                        addr.getId(), addr.getAddress(), addr.getProvince(), addr.getDistrict(), addr.getWard()
                    )
                );

            UserResponse updateUserDTO = userMapper.toDto(this.userRepository.saveAndFlush(updateUser));
            updateUserDTO.setAddresses(addresses);
            return updateUserDTO;
        } catch (ServiceException e){
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteUserById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUserById'");
    }

    private void checkExistUser(String userName, String userEmail, String userPhoneNumber, UUID excludeId){
        Optional<User> duplicate;
    
        if (excludeId == null) {
            duplicate = userRepository.findDuplicateForCreate(userName, userEmail, userPhoneNumber);
        } else {
            duplicate = userRepository.findDuplicateForUpdate(userName, userEmail, userPhoneNumber, excludeId);
        }

        duplicate.ifPresent(user -> {
            if (user.getEmail().equals(userEmail)) 
                throw new ServiceException(EnumError.IDENTITY_USER_DATA_EXISTED_EMAIL,"user.exist.email", Map.of("email", user.getEmail()));
            if (user.getPhoneNumber().equals(userPhoneNumber)) 
                throw new ServiceException(EnumError.IDENTITY_USER_DATA_EXISTED_PHONE_NUMBER,"user.exist.phoneNumber", Map.of("phoneNumber", user.getPhoneNumber()));
        });
    }
}
