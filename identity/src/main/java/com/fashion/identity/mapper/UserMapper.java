package com.fashion.identity.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.identity.dto.request.UserRequest;
import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.dto.response.UserResponse.InnerUserResponse;
import com.fashion.identity.entity.User;

@Mapper(
    componentModel = "spring",
    uses = {
        RoleMapper.class,
        AddressMapper.class
    }
)
public interface UserMapper extends EntityMapper<UserResponse, User, InnerUserResponse> {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Named("toDto")
    UserResponse toDto(User entity);
    List<UserResponse> toDto(List<User> entity);

    @Named("toDtoNotRelationship")
    @Mapping(target = "role", ignore = true)
    UserResponse toDtoNotRelationship(User entity);

    @Named("toEntity")
    User toEntity(UserResponse dto);
    List<User> toEntity(List<UserResponse> dto);

    @Named("toInnerEntity")
    InnerUserResponse toInnerEntity(User entity);
    List<InnerUserResponse> toInnerEntity(List<User> entity);

    @Named("toValidated")
    User toValidated(UserRequest dto);
}
