package com.fashion.order.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fashion.order.common.enums.OrderEnum;
import com.fashion.order.common.enums.ShippingProvider;
import com.fashion.order.dto.request.CouponRequest.InnerCouponRequest;
import com.fashion.order.dto.request.OrderDetailRequest.InnerOrderDetailRequest;
import com.fashion.order.dto.request.OrderDetailRequest.InnerOrderDetail_FromOrderRequest;
import com.fashion.order.dto.request.internal.AddressRequest.InnerAddressRequest;
import com.fashion.order.dto.request.internal.UserRequest.InnerUserRequest;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class OrderRequest {
    public interface Create {}
    public interface Update {}

    @NotNull(groups = Update.class, message = "order.id.notNull")
    UUID id;

    @NotNull(groups = {Create.class, Update.class}, message = "order.totalItem.notNull")
    Integer totalItem;
    
    @NotNull(groups = {Create.class, Update.class}, message = "order.paymentMethod.notNull")
    String paymentMethod;

    // @NotNull(groups = {Create.class, Update.class}, message = "order.totalPrice.notNull")
    BigDecimal totalPrice;

    @Valid
    InnerUserRequest user;
    
    @Valid
    InnerAddressRequest address;

    @Valid
    List<InnerOrderDetail_FromOrderRequest> orderDetails;
    
    @NotNull(groups = Update.class, message = "order.version.notSimilar.currentVersion")
    Long dbVersion;
    
    @NotNull(groups ={Create.class, Update.class}, message = "order.version.notNull")
    Long version;

    @NotNull(groups = {Create.class, Update.class}, message = "order.shippingProvider.notNull")
    ShippingProvider shippingProvider;

    InnerCouponRequest coupon;

    @Enumerated(EnumType.STRING)
    OrderEnum status;
    BigDecimal finalPrice;
    BigDecimal discountPrice;
    String shippingStatus;
    String paymentStatus;
    BigDecimal shippingFee;
    UUID shippingId;
    UUID paymentId;

    String note;


    Boolean activated;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerOrderRequest {
        UUID id;
    }
}
