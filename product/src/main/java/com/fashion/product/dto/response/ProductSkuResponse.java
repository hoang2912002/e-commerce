package com.fashion.product.dto.response;

import java.time.Instant;
import java.util.UUID;

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
    Double price;
    Integer tempStock;
    
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
        Double price;
        int stock;
    }
}
