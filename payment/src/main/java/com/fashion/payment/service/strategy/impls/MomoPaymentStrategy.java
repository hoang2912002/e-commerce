package com.fashion.payment.service.strategy.impls;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import com.fashion.payment.common.enums.EnumError;
import com.fashion.payment.common.enums.PaymentEnum;
import com.fashion.payment.dto.request.thirdParty.MomoRequest;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.dto.response.PaymentTransactionResponse;
import com.fashion.payment.dto.response.PaymentTransactionResponse.InnerPaymentTransactionResponse;
import com.fashion.payment.entity.Payment;
import com.fashion.payment.entity.PaymentTransaction;
import com.fashion.payment.exception.ServiceException;
import com.fashion.payment.mapper.PaymentMapper;
import com.fashion.payment.properties.PaymentMomoProperties;
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
public class MomoPaymentStrategy implements PaymentStrategy {
    PaymentMapper paymentMapper;
    PaymentTransactionRepository paymentTransactionRepository;
    PaymentMomoProperties paymentMomoProperties;

    @Override
    public PaymentResponse process(Payment payment) {
        if ( payment == null ) {
            throw new ServiceException(EnumError.PAYMENT_PAYMENT_PROCESS_NOT_NULL, "payment.data.notNull");
        }
        String mockRedirectUrl = String.format(
            paymentMomoProperties.getEndPoint(), 
            payment.getOrderId(), 
            payment.getAmount()
        );
        PaymentTransaction successTrans = PaymentTransaction.builder()
            .payment(payment)
            .eventId(UUID.randomUUID())
            .transactionId("MOMO_SUCCESS_" + UUID.randomUUID()) // Giả lập mã GD thành công
            .status(PaymentEnum.SUCCESS)
            .rawResponse(mockRedirectUrl)
            .note("Force Success after creating link")
            .build();
        this.paymentTransactionRepository.save(successTrans);
        payment.setStatus(PaymentEnum.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        return this.paymentMapper.toDto(payment);
    }

    @Override
    public String getPaymentMethod() {
        return paymentMomoProperties.getPartnerCode().toLowerCase();
    }

}
