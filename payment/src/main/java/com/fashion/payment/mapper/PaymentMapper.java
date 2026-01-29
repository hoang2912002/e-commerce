package com.fashion.payment.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.payment.dto.request.PaymentRequest;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.dto.response.PaymentResponse.InnerPaymentResponse;
import com.fashion.payment.entity.Payment;

@Mapper(
    componentModel = "spring",
    uses = {
        PaymentMethodMapper.class,
        PaymentTransactionMapper.class
    }
)
public interface PaymentMapper extends EntityMapper<PaymentResponse, Payment, InnerPaymentResponse, PaymentRequest>{
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Named("toDto")
    PaymentResponse toDto(Payment entity);
    List<PaymentResponse> toDto(List<Payment> entity);

    @Named("toDtoNotRelationship")
    PaymentResponse toDtoNotRelationship(Payment entity);

    @Named("toEntity")
    Payment toEntity(PaymentResponse dto);
    List<Payment> toEntity(List<PaymentResponse> dto);

    @Named("toInnerEntity")
    InnerPaymentResponse toInnerEntity(Payment entity);
    List<InnerPaymentResponse> toInnerEntity(List<Payment> entity);

    @Named("toValidated")
    Payment toValidated(PaymentRequest dto);
}
