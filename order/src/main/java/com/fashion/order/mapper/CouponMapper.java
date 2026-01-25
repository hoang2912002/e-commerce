package com.fashion.order.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.order.dto.request.CouponRequest;
import com.fashion.order.dto.response.CouponResponse;
import com.fashion.order.dto.response.CouponResponse.InnerCouponResponse;
import com.fashion.order.entity.Coupon;


@Mapper(
    componentModel = "spring"
)
public interface CouponMapper extends EntityMapper<CouponResponse,Coupon,InnerCouponResponse,CouponRequest>{
    CouponMapper INSTANCE = Mappers.getMapper(CouponMapper.class);
    
    @Named("toDto")
    CouponResponse toDto(Coupon entity);
    List<CouponResponse> toDto(List<Coupon> entity);

    @Named("toDtoNotRelationship")
    CouponResponse toDtoNotRelationship(Coupon entity);

    @Named("toEntity")
    Coupon toEntity(CouponResponse dto);
    List<Coupon> toEntity(List<CouponResponse> dto);

    @Named("toInnerEntity")
    InnerCouponResponse toInnerEntity(Coupon entity);
    List<InnerCouponResponse> toInnerEntity(List<Coupon> entity);

    @Named("toValidated")
    @Mapping(target = "version", ignore = true)
    Coupon toValidated(CouponRequest dto);

    @Named("toUpdate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    // @Mapping(target = "updatedAt", ignore = true)
    // @Mapping(target = "updatedBy", ignore = true)
    void toUpdate(@MappingTarget Coupon entity, CouponRequest dto);
}
