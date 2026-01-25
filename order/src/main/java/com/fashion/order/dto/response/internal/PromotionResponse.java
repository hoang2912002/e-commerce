package com.fashion.order.dto.response.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fashion.order.common.enums.PromotionEnum;
import com.fashion.order.dto.response.internal.ProductResponse.InnerProductResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PromotionResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerPromotionResponse {
        UUID id;
        String code;
        String name;
        BigDecimal discountPercent;
        BigDecimal minDiscountAmount;
        BigDecimal maxDiscountAmount;
        Integer quantity;
        PromotionEnum discountType;
        LocalDate startDate;
        LocalDate endDate;
        byte optionPromotion;
        BigDecimal discountFinal;
    }
}
