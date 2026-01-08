package com.fashion.identity.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.context.properties.bind.Name;

import com.fashion.identity.dto.request.PermissionRequest;
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

    @Named("toValidated")
    Permission toValidated(PermissionRequest dto); 

    @Named("toUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toUpdate(@MappingTarget Permission entity, PermissionRequest dto);
}
