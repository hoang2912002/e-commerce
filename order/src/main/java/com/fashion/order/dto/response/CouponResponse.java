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
public class CouponResponse extends VersionResponse{
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerLuaCouponResponse {
        UUID id;
        Boolean success;
        String errorType; // NOT_FOUND, INSUFFICIENT, ERROR
        String errorMessage;
        Integer beforeStock;
        Integer afterStock;
        Integer deductionStock;

        public static InnerLuaCouponResponse success(UUID id, Integer beforeStock, Integer afterStock) {
            return InnerLuaCouponResponse.builder()
                .id(id)
                .success(true)
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .build();
        }

        public static InnerLuaCouponResponse notFound(UUID skuId) {
            return InnerLuaCouponResponse.builder()
                .id(skuId)
                .success(false)
                .errorType("NOT_FOUND")
                .errorMessage("Inventory not found in cache")
                .build();
        }
        
        public static InnerLuaCouponResponse insufficient(UUID skuId, Integer current, Integer required) {
            return InnerLuaCouponResponse.builder()
                .id(skuId)
                .success(false)
                .errorType("INSUFFICIENT")
                .errorMessage("Insufficient inventory")
                .beforeStock(current)
                .deductionStock(required)
                .build();
        }
        
        public static InnerLuaCouponResponse error(UUID skuId, String message) {
            return InnerLuaCouponResponse.builder()
                .id(skuId)
                .success(false)
                .errorType("ERROR")
                .errorMessage(message)
                .build();
        }
    }
}
