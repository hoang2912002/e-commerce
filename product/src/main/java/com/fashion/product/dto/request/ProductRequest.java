package com.fashion.product.dto.request;

import java.util.UUID;

import com.fashion.product.dto.request.CategoryRequest.InnerCategoryRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class ProductRequest {
    UUID id;
    String name;
    Double price;
    String thumbnail;
    // Integer quantity;
    
    String description;
    InnerCategoryRequest category;
    // InnerShopManagementRequest shopManagement;

    // List<InnerVariantRequest> variants;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerProductRequest {
        UUID id;
    }
}
