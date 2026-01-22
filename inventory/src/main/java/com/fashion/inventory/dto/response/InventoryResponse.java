package com.fashion.inventory.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.fashion.inventory.dto.request.WareHouseRequest.InnerWareHouseRequest;
import com.fashion.inventory.dto.response.WareHouseResponse.InnerWareHouseResponse;
import com.fashion.inventory.dto.response.internal.ProductResponse.InnerProductResponse;
import com.fashion.inventory.dto.response.internal.ProductSkuResponse.InnerProductSkuResponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
    UUID id;
    Integer quantityAvailable;
    Integer quantityReserved;
    Integer quantitySold;
    UUID productId;
    UUID productSkuId;
    InnerWareHouseResponse warehouse;
    InnerProductResponse product;
    InnerProductSkuResponse productSku;
    Boolean activated;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerInventoryResponse {
        UUID id;
        Integer quantityAvailable;
        Integer quantityReserved;
        Integer quantitySold;
    }
}
