package com.fashion.payment.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fashion.payment.common.enums.PaymentEnum;
import com.fashion.payment.dto.request.PaymentMethodRequest.InnerPaymentMethodRequest;
import com.fashion.payment.dto.request.PaymentTransactionRequest.InnerPaymentTransactionRequest;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class PaymentRequest {
    public interface Create {};
    public interface Update {};

    @NotNull(groups = Update.class, message = "payment.id.notNull")
    UUID id;

    @NotNull(groups = {Create.class,Update.class}, message = "payment.amount.notNull")
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    PaymentEnum status;

    @NotNull(groups = {Create.class,Update.class}, message = "payment.orderId.notNull")
    UUID orderId;

    LocalDateTime paidAt;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;

    @Valid
    InnerPaymentMethodRequest paymentMethod;

    List<InnerPaymentTransactionRequest> paymentTransaction;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerPaymentRequest {
        UUID id;
    }
}
