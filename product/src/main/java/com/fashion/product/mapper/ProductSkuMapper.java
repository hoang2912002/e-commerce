package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.ProductRequest;
import com.fashion.product.dto.request.ProductSkuRequest;
import com.fashion.product.dto.response.ProductSkuResponse;
import com.fashion.product.dto.response.ProductSkuResponse.InnerProductSkuResponse;
import com.fashion.product.entity.ProductSku;

@Mapper(componentModel = "spring")
public interface ProductSkuMapper extends EntityMapper<ProductSkuResponse, ProductSku, InnerProductSkuResponse, ProductSkuRequest>{
    ProductSkuMapper INSTANCE = Mappers.getMapper(ProductSkuMapper.class);

    @Named("toDto")
    ProductSkuResponse toDto(ProductSku entity);
    List<ProductSkuResponse> toDto(List<ProductSku> entity);

    @Named("toDtoNotRelationship")
    ProductSkuResponse toDtoNotRelationship(ProductSku entity);

    @Named("toEntity")
    ProductSku toEntity(ProductSkuResponse dto);
    List<ProductSku> toEntity(List<ProductSkuResponse> dto);

    @Named("toInnerEntity")
    InnerProductSkuResponse toInnerEntity(ProductSku entity);
    List<InnerProductSkuResponse> toInnerEntity(List<ProductSku> entity);

    @Named("toValidated")
    ProductSku toValidated(ProductRequest dto);

    @Named("toUpdate")
    void toUpdate(@MappingTarget ProductSku entity, ProductRequest dto);
}
