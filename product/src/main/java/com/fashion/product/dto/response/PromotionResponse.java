package com.fashion.product.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fashion.product.common.enums.PromotionEnum;
import com.fashion.product.dto.response.CategoryResponse.InnerCategoryResponse;
import com.fashion.product.dto.response.ProductResponse.InnerProductResponse;

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
public class PromotionResponse {
    UUID id;
    String code;
    String name;
    String description;
    Double discountPercent;
    Double minDiscountAmount;
    Double maxDiscountAmount;
    Integer quantity;
    PromotionEnum discountType;
    LocalDate startDate;
    LocalDate endDate;
    byte optionPromotion;

    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;

    Double discountFinal; // giá giảm cuối cùng (giá sản phẩm - (discountPercent | minDiscountAmount))

    List<InnerProductResponse> products;
    List<InnerCategoryResponse> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerPromotionResponse {
        UUID id;
        String code;
        String name;
        Double discountPercent;
        Double minDiscountAmount;
        Double maxDiscountAmount;
        Integer quantity;
        PromotionEnum discountType;
    }
}
