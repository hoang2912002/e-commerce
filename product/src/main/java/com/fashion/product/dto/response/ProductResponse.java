package com.fashion.product.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.response.CategoryResponse.InnerCategoryResponse;
import com.fashion.product.dto.response.OptionResponse.InnerOptionResponse;
import com.fashion.product.dto.response.OptionValueResponse.InnerOptionValueResponse;
import com.fashion.product.dto.response.ProductSkuResponse.InnerProductSkuResponse;
import com.fashion.product.dto.response.PromotionResponse.InnerPromotionResponse;
import com.fashion.product.dto.response.ShopManagementResponse.InnerShopManagementResponse;

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
public class ProductResponse extends VersionResponse{
    UUID id;
    String name;
    String price;
    String thumbnail;
    Integer quantity;
    
    String description;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;
    InnerCategoryResponse category;
    InnerShopManagementResponse shopManagement;
    List<InnerProductSkuResponse> productSkus;
    List<InnerOptionResponse> options;
    List<InnerOptionValueResponse> optionValues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerProductResponse {
        UUID id;
        String name;
    }
}
