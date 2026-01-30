package com.fashion.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fashion.payment.common.enums.PaymentEnum;
import com.fashion.payment.dto.response.PaymentMethodResponse.InnerPaymentMethodResponse;
import com.fashion.payment.dto.response.PaymentTransactionResponse.InnerPaymentTransactionResponse;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class PaymentResponse {
    UUID id;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    PaymentEnum status;
    UUID orderId;

    LocalDateTime paidAt;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;
    InnerPaymentMethodResponse paymentMethod;
    List<InnerPaymentTransactionResponse> paymentTransactions;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerPaymentResponse {
        UUID id;
        BigDecimal amount;
        UUID orderId;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerInternalPayment {
        UUID id;
        BigDecimal amount;
        UUID orderId;
        String paymentMethod;
        PaymentEnum status;
    }
}
