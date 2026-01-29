package com.fashion.payment.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.fashion.payment.common.enums.PaymentEnum;
import com.fashion.payment.common.enums.PaymentMethodEnum;
import com.fashion.payment.dto.response.PaymentResponse.InnerPaymentResponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactionRequest {
    Long id;
    UUID eventId;
    String transactionId;
    PaymentEnum status;
    String rawResponse;
    String note;
    InnerPaymentResponse payment;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerPaymentTransactionRequest {
        Long id;
        UUID eventId;
        String transactionId;
        PaymentEnum status;
        String rawResponse;
        String note;
    }
}
