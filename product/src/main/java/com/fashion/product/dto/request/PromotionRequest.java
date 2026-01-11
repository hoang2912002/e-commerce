package com.fashion.product.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fashion.product.common.enums.PromotionEnum;
import com.fashion.product.dto.request.CategoryRequest.InnerCategoryRequest;
import com.fashion.product.dto.request.ProductRequest.InnerProductRequest;
import com.fashion.product.validator.promotion.PromotionMatching;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@PromotionMatching(groups = {PromotionRequest.Create.class, PromotionRequest.Update.class})
public class PromotionRequest {
    public interface Create{};
    public interface Update{};
    
    @NotNull(message = "promotion.id.notNull", groups = Update.class)
    UUID id;
    String code;
    String name;
    String description;
    Integer discountPercent;
    BigDecimal minDiscountAmount;
    BigDecimal maxDiscountAmount;
    Integer quantity;
    PromotionEnum discountType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    LocalDateTime endDate;
    byte optionPromotion;

    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;

    List<InnerProductRequest> products;
    List<InnerCategoryRequest> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerPromotionRequest {
        UUID id;
        String code;
    }
}
