package com.fashion.product.dto.request;

import java.time.Instant;

import com.fashion.product.dto.request.ProductRequest.InnerProductRequest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSkuRequest {
    Long id;
    String sku;
    Double price;
    Integer tempStock;
    String thumbnail;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;
    InnerProductRequest product;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerProductSkuRequest {
        Long id;
    }
}
