package com.fashion.order.dto.request.internal;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

public class InventoryRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerInventoryRequest {
        UUID id;
        Integer quantity;
        Integer quantityReserved;
        Integer quantitySold;
        String referenceType;
        Long referenceId;
    }
}
