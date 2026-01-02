package com.fashion.identity.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.identity.dto.response.PermissionResponse;
import com.fashion.identity.dto.response.PermissionResponse.InnerPermissionResponse;
import com.fashion.identity.entity.Permission;

@Mapper(
    componentModel = "spring"
)
public interface PermissionMapper extends EntityMapper<PermissionResponse, Permission, InnerPermissionResponse> {
    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    @Named("toDto")
    PermissionResponse toDto(Permission entity);
    List<PermissionResponse> toDto(List<Permission> entity);

    @Named("toEntity")
    Permission toEntity(PermissionResponse dto);
    List<Permission> toEntity(List<PermissionResponse> dto);

    @Named("toInnerEntity")
    InnerPermissionResponse toInnerEntity(Permission entity);
    List<InnerPermissionResponse> toInnerEntity(List<Permission> entity);
}
