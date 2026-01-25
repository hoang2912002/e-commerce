package com.fashion.order.dto.request.internal;

import java.util.UUID;

import com.fashion.order.dto.request.OrderRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

public class ProductSkuRequest {
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerProductSkuRequest {
        @NotNull(groups = {OrderRequest.Create.class, OrderRequest.Update.class}, message = "order.orderDetail.productSkuId.notNull")
        UUID id;
    }
}
