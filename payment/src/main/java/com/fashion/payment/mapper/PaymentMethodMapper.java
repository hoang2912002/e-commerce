package com.fashion.payment.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.payment.dto.request.PaymentMethodRequest;
import com.fashion.payment.dto.request.PaymentRequest;
import com.fashion.payment.dto.response.PaymentMethodResponse;
import com.fashion.payment.dto.response.PaymentMethodResponse.InnerPaymentMethodResponse;
import com.fashion.payment.entity.PaymentMethod;

@Mapper(
    componentModel = "spring",
    uses = {
        
    }
)
public interface PaymentMethodMapper extends EntityMapper<PaymentMethodResponse, PaymentMethod, InnerPaymentMethodResponse, PaymentMethodRequest>{
    PaymentMethodMapper INSTANCE = Mappers.getMapper(PaymentMethodMapper.class);

    @Named("toDto")
    PaymentMethodResponse toDto(PaymentMethod entity);
    List<PaymentMethodResponse> toDto(List<PaymentMethod> entity);

    @Named("toDtoNotRelationship")
    PaymentMethodResponse toDtoNotRelationship(PaymentMethod entity);

    @Named("toEntity")
    PaymentMethod toEntity(PaymentMethodResponse dto);
    List<PaymentMethod> toEntity(List<PaymentMethodResponse> dto);

    @Named("toInnerEntity")
    InnerPaymentMethodResponse toInnerEntity(PaymentMethod entity);
    List<InnerPaymentMethodResponse> toInnerEntity(List<PaymentMethod> entity);

    @Named("toValidated")
    PaymentMethod toValidated(PaymentMethodRequest dto);

    @Named("toUpdate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void toUpdate(@MappingTarget PaymentMethod entity, PaymentMethodRequest dto);
}
