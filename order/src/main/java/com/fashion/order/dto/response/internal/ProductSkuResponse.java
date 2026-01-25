package com.fashion.order.dto.response.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fashion.order.dto.response.internal.ProductResponse.InnerProductResponse;
import com.fashion.order.dto.response.internal.PromotionResponse.InnerPromotionResponse;

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
public class ProductSkuResponse {
    UUID id;
    String sku;
    BigDecimal price;
    int tempStock;
    InnerProductResponse product;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerProductSkuResponse {
        UUID id;
        String sku;
        BigDecimal price;
        int tempStock;
        InnerPromotionResponse promotion;
    }
}
