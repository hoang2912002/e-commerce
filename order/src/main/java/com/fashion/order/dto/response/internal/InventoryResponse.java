package com.fashion.order.dto.response.internal;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

public class InventoryResponse {
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
