package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.ShopManagementRequest;
import com.fashion.product.dto.response.ShopManagementResponse;
import com.fashion.product.dto.response.ShopManagementResponse.InnerShopManagementResponse;
import com.fashion.product.entity.ShopManagement;
@Mapper(
    componentModel = "spring",
    uses = {
    }
)
public interface ShopManagementMapper extends EntityMapper<ShopManagementResponse,ShopManagement,InnerShopManagementResponse, ShopManagementRequest>{
    ShopManagementMapper INSTANCE = Mappers.getMapper(ShopManagementMapper.class);
    
    @Named("toDto")
    ShopManagementResponse toDto(ShopManagement entity);
    List<ShopManagementResponse> toDto(List<ShopManagement> entity);

    @Named("toDtoNotRelationship")
    ShopManagementResponse toDtoNotRelationship(ShopManagement entity);

    @Named("toEntity")
    ShopManagement toEntity(ShopManagementResponse dto);
    List<ShopManagement> toEntity(List<ShopManagementResponse> dto);

    @Named("toInnerEntity")
    InnerShopManagementResponse toInnerEntity(ShopManagement entity);
    List<InnerShopManagementResponse> toInnerEntity(List<ShopManagement> entity);

    @Named("toValidated")
    @Mapping(source = "user.id", target = "userId")
    ShopManagement toValidated(ShopManagementRequest dto);
}
