package com.fashion.shipping.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.shipping.common.enums.ShippingEnum;
import com.fashion.shipping.common.enums.ShippingProvider;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingRequest {
    public interface Create {};
    public interface Update {};

    @NotNull(groups = Update.class, message = "shipping.id.notNull")
    UUID id;

    // @NotNull(groups = {Create.class, Update.class}, message = "shipping.deliveredAt.notNull")
    LocalDateTime deliveredAt;

    // @NotNull(groups = {Create.class, Update.class}, message = "shipping.estimatedDate.notNull")
    LocalDateTime estimatedDate;

    @NotNull(groups = {Create.class, Update.class}, message = "shipping.provider.notNull")
    @Enumerated(EnumType.STRING)
    ShippingProvider provider;

    // @NotNull(groups = {Create.class, Update.class}, message = "shipping.shippingAt.notNull")
    LocalDateTime shippingAt;

    // @NotNull(groups = {Create.class, Update.class}, message = "shipping.shippingFee.notNull")
    BigDecimal shippingFee;

    @NotNull(groups = {Create.class, Update.class}, message = "shipping.status.notNull")
    @Enumerated(EnumType.STRING)
    ShippingEnum status;
    
    Long version;

    UUID eventId;

    String trackingCode;

    @NotNull(groups = {Create.class, Update.class}, message = "shipping.orderId.notNull")
    UUID orderId;

    @NotNull(groups = {Create.class, Update.class}, message = "shipping.orderCode.notNull")
    String orderCode;

    @NotNull(groups = {Create.class, Update.class}, message = "shipping.orderCreatedAt.notNull")
    Instant orderCreatedAt;
    Boolean activated;
    
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
}
