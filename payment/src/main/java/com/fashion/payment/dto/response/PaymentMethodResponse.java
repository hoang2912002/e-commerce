package com.fashion.payment.dto.response;

import java.time.Instant;

import com.fashion.payment.common.enums.PaymentMethodEnum;

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
public class PaymentMethodResponse extends VersionResponse {
    Long id;
    String code;
    String name;
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
    public static class InnerPaymentMethodResponse {
        Long id;
        String code;
        String name;
        PaymentMethodEnum status;
    }
}
