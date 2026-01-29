package com.fashion.payment.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.fashion.payment.common.enums.PaymentEnum;
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
public class PaymentTransactionResponse {
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
    public static class InnerPaymentTransactionResponse {
        Long id;
        UUID eventId;
        String transactionId;
        PaymentEnum status;
        String rawResponse;
        String note;
    }
}
