package com.fashion.shipping.dto.response.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fashion.shipping.common.enums.OrderEnum;
import com.fashion.shipping.dto.response.internal.CouponResponse.InnerCouponResponse;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class OrderResponse {
    UUID id;
    String paymentStatus;
    String shippingStatus;

    @Enumerated(EnumType.STRING)
    OrderEnum status;

    Integer totalItem;
    BigDecimal totalPrice;
    BigDecimal discountPrice;
    BigDecimal finalPrice;
    BigDecimal shippingFee;
    UUID userId;
    UUID shippingId;
    UUID addressId;
    UUID paymentId;
    String note;
    Long version;
    InnerCouponResponse coupon;

    String receiverName;
    String receiverEmail;
    String receiverPhone;
    String receiverAddress;
    String receiverProvince;
    String receiverDistrict;
    String receiverWard;
    
    Boolean activated;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
}
