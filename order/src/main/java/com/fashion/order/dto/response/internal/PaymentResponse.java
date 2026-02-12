package com.fashion.order.dto.response.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.order.common.enums.PaymentEnum;

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
    String paymentMethod;
    String transactionId;
    @Enumerated(EnumType.STRING)
    PaymentEnum status;
    UUID orderId;
    String note;
    String redirectUrl;

    LocalDateTime paidAt;
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
    public static class InnerInternalPayment {
        UUID id;
        BigDecimal amount;
        UUID orderId;
        String paymentMethod;
        PaymentEnum status;
    }
}
