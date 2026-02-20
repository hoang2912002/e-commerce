package com.fashion.shipping.dto.response.kafka;

import java.util.UUID;

import com.fashion.shipping.common.enums.PaymentEnum;
import com.fashion.shipping.common.enums.ShippingEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
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

    public static SagaStateResponse success(UUID shippingId, ShippingEnum shippingStatus) {
        return SagaStateResponse.builder().success(true).shippingId(shippingId).shippingStatus(shippingStatus).build();
    }

    public static SagaStateResponse failure(UUID shippingId, ShippingEnum shippingStatus, String error) {
        return SagaStateResponse.builder().success(false).shippingId(shippingId).shippingStatus(shippingStatus).errorMessage(error).build();
    }
}
