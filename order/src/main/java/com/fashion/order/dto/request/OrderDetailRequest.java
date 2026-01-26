package com.fashion.order.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fashion.order.dto.request.OrderRequest.InnerOrderRequest;
import com.fashion.order.dto.request.internal.ProductRequest.InnerProductRequest;
import com.fashion.order.dto.request.internal.ProductSkuRequest.InnerProductSkuRequest;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailRequest {
    public interface Create {}
    public interface Update {}

    Long id;
    BigDecimal priceOriginal;
    BigDecimal price;
    BigDecimal promotionDiscount;
    Integer quantity;
    BigDecimal totalPrice;
    Boolean activated;
    UUID productId;
    UUID productSkuId;
    InnerOrderRequest order;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerOrderDetailRequest {
        Long id;        
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InnerOrderDetail_FromOrderRequest {
        // @NotNull(groups = OrderRequest.Update.class, message = "order.detail.id.notNull")
        @Builder.Default
        Long id = 0L;
        @NotNull(groups = {OrderRequest.Update.class, OrderRequest.Update.class}, message = "order.orderDetail.quantity.notNull")
        Integer quantity;
        @Valid
        InnerProductRequest product;
        @Valid
        InnerProductSkuRequest productSku;
    }
}
