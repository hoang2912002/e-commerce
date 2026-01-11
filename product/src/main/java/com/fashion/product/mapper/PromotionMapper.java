package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.PromotionRequest;
import com.fashion.product.dto.response.PromotionResponse;
import com.fashion.product.dto.response.PromotionResponse.InnerPromotionResponse;
import com.fashion.product.entity.Promotion;
@Mapper(
    componentModel = "spring",
    uses = {
        CategoryMapper.class
    }
)
public interface PromotionMapper extends EntityMapper<PromotionResponse, Promotion, InnerPromotionResponse, PromotionRequest>{
    PromotionMapper INSTANCE = Mappers.getMapper(PromotionMapper.class);
    
    @Named("toDto")
    PromotionResponse toDto(Promotion entity);
    List<PromotionResponse> toDto(List<Promotion> entity);

    @Named("toDtoNotRelationship")
    PromotionResponse toDtoNotRelationship(Promotion entity);

    @Named("toEntity")
    Promotion toEntity(PromotionResponse dto);
    List<Promotion> toEntity(List<PromotionResponse> dto);

    @Named("toInnerEntity")
    InnerPromotionResponse toInnerEntity(Promotion entity);
    List<InnerPromotionResponse> toInnerEntity(List<Promotion> entity);

    @Named("toValidated")
    Promotion toValidated(PromotionRequest dto);
}
