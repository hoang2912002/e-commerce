package com.fashion.product.dto.response.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.product.common.enums.ShippingEnum;

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
public class ShippingResponse {
    UUID id;
    LocalDateTime deliveredAt;
    LocalDateTime estimatedTime;
    String provider;
    LocalDateTime shippingAt;
    BigDecimal shippingFee;
    ShippingEnum status;
    String trackingCode;
    UUID orderId;
    Boolean activated;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
}
