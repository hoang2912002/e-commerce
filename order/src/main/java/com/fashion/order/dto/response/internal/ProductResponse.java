package com.fashion.order.dto.response.internal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fashion.order.dto.response.VersionResponse;
import com.fashion.order.dto.response.internal.ProductSkuResponse.InnerProductSkuResponse;
import com.fashion.order.dto.response.internal.PromotionResponse.InnerPromotionResponse;
import com.fashion.order.dto.response.internal.ShopManagementResponse.InnerShopManagementResponse;

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

    InnerShopManagementResponse shopManagement;
    List<InnerProductSkuResponse> productSkus;

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
