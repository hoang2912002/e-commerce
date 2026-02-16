package com.fashion.payment.dto.response.kafka;

import java.util.UUID;

import com.fashion.payment.common.enums.PaymentEnum;
import com.fashion.payment.common.enums.ShippingEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SagaStateResponse {
    boolean success;
    String errorMessage;
    UUID paymentId;
    PaymentEnum paymentStatus;
    UUID shippingId;
    ShippingEnum shippingStatus;

    public static SagaStateResponse success() {
        return SagaStateResponse.builder().success(true).build();
    }
    
    public static SagaStateResponse failure(String error) {
        return SagaStateResponse.builder().success(false).errorMessage(error).build();
    }

    public static SagaStateResponse successPayment(UUID paymentId, PaymentEnum paymentStatus) {
        return SagaStateResponse.builder().success(true).paymentId(paymentId).paymentStatus(paymentStatus).build();
    }
    
    public static SagaStateResponse failurePayment(UUID paymentId, PaymentEnum paymentStatus, String error) {
        return SagaStateResponse.builder().success(false).paymentId(paymentId).paymentStatus(paymentStatus).errorMessage(error).build();
    }
}

