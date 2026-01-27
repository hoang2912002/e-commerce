package com.fashion.payment.dto.response.internal;

import java.time.Instant;
import java.util.UUID;

import com.fashion.payment.dto.response.internal.WareHouseResponse.InnerWareHouseResponse;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ReturnAvailableQuantity {
        UUID productId;
        UUID productSkuId;
        // Số lượng luân chuyển
        Integer circulationCount;
        // sl giữa mới > sl giữ cũ
        boolean isNegative;
    }
}
