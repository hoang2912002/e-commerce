package com.fashion.order.dto.request.internal;

import java.util.List;
import java.util.UUID;

import com.fashion.order.dto.request.OrderRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

public class ProductRequest {
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerProductRequest {
        @NotNull(groups = {OrderRequest.Create.class, OrderRequest.Update.class}, message = "order.orderDetail.productId.notNull")
        UUID id;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerInternalProductRequest {
        List<UUID> productIdList;
        List<UUID> productSkuIdList;
        
    }
}
