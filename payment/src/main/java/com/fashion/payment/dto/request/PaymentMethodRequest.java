package com.fashion.payment.dto.request;

import java.time.Instant;

import com.fashion.payment.common.enums.PaymentMethodEnum;

import jakarta.validation.constraints.NotBlank;
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
public class PaymentMethodRequest {
    public interface Create {};
    public interface Update {};

    @NotNull(groups = Update.class, message = "payment.method.id.notNull")
    Long id;

    @NotBlank(groups = {Create.class,Update.class}, message = "payment.method.code.notNull")
    String code;
    @NotBlank(groups = {Create.class,Update.class}, message = "payment.method.name.notNull")
    String name;
    
    @NotNull(groups = Update.class, message = "payment.method.status.notNull")
    PaymentMethodEnum status;
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
    public static class InnerPaymentMethodRequest {
        @NotNull(groups = {PaymentRequest.Create.class, PaymentRequest.Update.class}, message = "payment.method.id.notNull")
        Long id;
        String code;
    }
}
