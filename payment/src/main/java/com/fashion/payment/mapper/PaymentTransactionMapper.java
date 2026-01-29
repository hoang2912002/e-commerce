package com.fashion.payment.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.payment.dto.request.PaymentTransactionRequest;
import com.fashion.payment.dto.response.PaymentTransactionResponse;
import com.fashion.payment.dto.response.PaymentTransactionResponse.InnerPaymentTransactionResponse;
import com.fashion.payment.entity.PaymentTransaction;

@Mapper(
    componentModel = "spring",
    uses = {
        
    }
)
public interface PaymentTransactionMapper extends EntityMapper<PaymentTransactionResponse, PaymentTransaction, InnerPaymentTransactionResponse, PaymentTransactionRequest>{
    PaymentTransactionMapper INSTANCE = Mappers.getMapper(PaymentTransactionMapper.class);

    @Named("toDto")
    PaymentTransactionResponse toDto(PaymentTransaction entity);
    List<PaymentTransactionResponse> toDto(List<PaymentTransaction> entity);

    @Named("toDtoNotRelationship")
    PaymentTransactionResponse toDtoNotRelationship(PaymentTransaction entity);

    @Named("toEntity")
    PaymentTransaction toEntity(PaymentTransactionResponse dto);
    List<PaymentTransaction> toEntity(List<PaymentTransactionResponse> dto);

    @Named("toInnerEntity")
    InnerPaymentTransactionResponse toInnerEntity(PaymentTransaction entity);
    List<InnerPaymentTransactionResponse> toInnerEntity(List<PaymentTransaction> entity);

    @Named("toValidated")
    PaymentTransaction toValidated(PaymentTransactionRequest dto);
}
