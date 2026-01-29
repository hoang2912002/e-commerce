package com.fashion.payment.service.strategy.impls;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fashion.payment.common.enums.EnumError;
import com.fashion.payment.common.enums.PaymentEnum;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.entity.Payment;
import com.fashion.payment.entity.PaymentTransaction;
import com.fashion.payment.exception.ServiceException;
import com.fashion.payment.mapper.PaymentMapper;
import com.fashion.payment.properties.PaymentCodProperties;
import com.fashion.payment.repository.PaymentTransactionRepository;
import com.fashion.payment.service.strategy.PaymentStrategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CodPaymentStrategy implements PaymentStrategy{
    PaymentCodProperties paymentCodProperties;
    PaymentTransactionRepository paymentTransactionRepository;
    PaymentMapper paymentMapper;

    @Override
    public PaymentResponse process(Payment payment) {
        if ( payment == null ) {
            throw new ServiceException(EnumError.PAYMENT_PAYMENT_PROCESS_NOT_NULL, "payment.data.notNull");
        }
        
        PaymentTransaction successTrans = PaymentTransaction.builder()
            .payment(payment)
            .eventId(UUID.randomUUID())
            .transactionId("COD_SUCCESS_" + UUID.randomUUID())
            .status(PaymentEnum.SUCCESS)
            .rawResponse(null)
            .note("Force Success after creating link")
            .build();
        this.paymentTransactionRepository.save(successTrans);
        payment.setStatus(PaymentEnum.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        return this.paymentMapper.toDto(payment);
    }

    @Override
    public String getPaymentMethod() {
        return this.paymentCodProperties.getPartnerCode().toLowerCase();
    }
}
