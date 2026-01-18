package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.ProductRequest;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.dto.response.ProductResponse.InnerProductResponse;
import com.fashion.product.entity.Product;
@Mapper(
    componentModel = "spring",
    uses = {
        CategoryMapper.class,
        ShopManagementMapper.class
    }
)
public interface ProductMapper extends EntityMapper<ProductResponse,Product, InnerProductResponse, ProductRequest>{
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Named("toDto")
    ProductResponse toDto(Product entity);
    List<ProductResponse> toDto(List<Product> entity);

    @Named("toDtoNotRelationship")
    // @Mapping(target = "shopManagement", ignore = true)
    // @Mapping(target = "promotionProducts", ignore = true)
    ProductResponse toDtoNotRelationship(Product entity);

    @Named("toEntity")
    Product toEntity(ProductResponse dto);
    List<Product> toEntity(List<ProductResponse> dto);

    @Named("toInnerEntity")
    InnerProductResponse toInnerEntity(Product entity);
    List<InnerProductResponse> toInnerEntity(List<Product> entity);

    @Named("toValidated")
    @Mapping(target = "variants", ignore = true)
    Product toValidated(ProductRequest dto);

    @Named("toUpdate")
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "shopManagement", ignore = true)
    void toUpdate(@MappingTarget Product entity, ProductRequest dto);
}
