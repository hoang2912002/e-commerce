package com.fashion.product.mapper;

import java.util.List;
import java.util.UUID;

import com.fashion.product.entity.Category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.common.util.ConvertUuidUtil;
import com.fashion.product.dto.request.CategoryRequest;
import com.fashion.product.dto.response.CategoryResponse;
import com.fashion.product.dto.response.CategoryResponse.InnerCategoryResponse;

@Mapper(
    componentModel = "spring"
)
public interface CategoryMapper extends EntityMapper<CategoryResponse,Category, InnerCategoryResponse>{
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Named("toDto")
    CategoryResponse toDto(Category entity);
    List<CategoryResponse> toDto(List<Category> entity);

    @Named("toDtoNotRelationship")
    @Mapping(target = "parent", ignore = true)
    CategoryResponse toDtoNotRelationship(Category entity);

    @Named("toEntity")
    Category toEntity(CategoryResponse dto);
    List<Category> toEntity(List<CategoryResponse> dto);

    @Named("toInnerEntity")
    InnerCategoryResponse toInnerEntity(Category entity);
    List<InnerCategoryResponse> toInnerEntity(List<Category> entity);

    @Named("toValidated")
    // @Mapping(target = "id", source = "id")
    Category toValidated(CategoryRequest dto);

    // Format id thành type UUID
    @Named("userToUuid")
    default UUID toUuid(Object id) {
        if (id == null) return null;
        return ConvertUuidUtil.toUuid(id);
    }
}
