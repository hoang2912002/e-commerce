package com.fashion.shipping.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.shipping.dto.request.ShippingRequest;
import com.fashion.shipping.dto.response.ShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerShippingResponse;
import com.fashion.shipping.dto.response.internal.PaymentResponse;
import com.fashion.shipping.entity.Shipping;

@Mapper(
    componentModel = "spring",
    uses = {}
)
public interface ShippingMapper extends EntityMapper<ShippingResponse, Shipping, InnerShippingResponse, ShippingRequest>{
    ShippingMapper INSTANCE = Mappers.getMapper(ShippingMapper.class);

    @Named("toDto")
    ShippingResponse toDto(Shipping entity);
    List<ShippingResponse> toDto(List<Shipping> entity);

    @Named("toDtoNotRelationship")
    ShippingResponse toDtoNotRelationship(Shipping entity);

    @Named("toEntity")
    Shipping toEntity(ShippingResponse dto);
    List<Shipping> toEntity(List<ShippingResponse> dto);

    @Named("toInnerEntity")
    InnerShippingResponse toInnerEntity(Shipping entity);
    List<InnerShippingResponse> toInnerEntity(List<Shipping> entity);

    @Named("toValidated")
    Shipping toValidated(ShippingRequest dto);
}
