package com.fashion.identity.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.identity.dto.response.RoleResponse;
import com.fashion.identity.dto.response.RoleResponse.InnerRoleResponse;
import com.fashion.identity.entity.Role;

@Mapper(
    componentModel = "spring",
    uses = {PermissionMapper.class}
)
public interface RoleMapper extends EntityMapper<RoleResponse, Role, InnerRoleResponse> {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    @Named("toDto")
    RoleResponse toDto(Role entity);
    List<RoleResponse> toDto(List<Role> entity);

    @Named("toEntity")
    Role toEntity(RoleResponse dto);
    List<Role> toEntity(List<RoleResponse> dto);

    @Named("toInnerEntity")
    InnerRoleResponse toInnerEntity(Role entity);
    List<InnerRoleResponse> toInnerEntity(List<Role> entity);

}
