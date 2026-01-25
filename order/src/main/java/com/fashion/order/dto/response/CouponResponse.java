package com.fashion.order.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.order.common.enums.CouponEnum;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponResponse {
    UUID id;
    String name;
    String code;
    @Enumerated(EnumType.STRING)
    CouponEnum type;
    Integer stock;
    LocalDateTime startDate;
    LocalDateTime endDate;
    BigDecimal couponAmount;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;
    Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerCouponResponse {
        UUID id;
        String name;
        String code;
        BigDecimal couponAmount;
    }
}
