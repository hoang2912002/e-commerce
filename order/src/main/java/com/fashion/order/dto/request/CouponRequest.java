package com.fashion.order.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.validation.annotation.Validated;

import com.fashion.order.common.enums.CouponEnum;
import com.fashion.order.validator.coupon.CouponMatching;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
@CouponMatching(groups = {CouponRequest.Create.class, CouponRequest.Update.class})
public class CouponRequest {
    public interface Create {}
    public interface Update {}

    @NotNull(groups = Update.class, message = "coupon.id.notNull")
    UUID id;

    @NotNull(groups = {Create.class,Update.class}, message = "coupon.name.notNull")
    String name;

    @NotNull(groups = {Create.class,Update.class}, message = "coupon.code.notNull")
    @Pattern(
        groups = {Create.class,Update.class},
        regexp = "^[A-Z0-9]+(?:-[A-Z0-9]+)*$", 
        message = "coupon.code.invalid.format"
    )
    String code;

    @NotNull(groups = {Create.class,Update.class}, message = "coupon.amount.notNull")
    BigDecimal couponAmount;

    @NotNull(groups = {Create.class,Update.class}, message = "coupon.startDate.notNull")
    LocalDateTime startDate;
    @NotNull(groups = {Create.class,Update.class}, message = "coupon.endDate.notNull")
    LocalDateTime endDate;

    @NotNull(groups = {Create.class,Update.class}, message = "coupon.stock.notNull")
    Integer stock;

    @NotNull(groups = {Create.class,Update.class}, message = "coupon.type.notNull")
    @Enumerated(EnumType.STRING)
    CouponEnum type;

    @NotNull(groups = {Update.class}, message = "coupon.version.notNull")
    Long version;

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
    public static class InnerCouponRequest {
        UUID id;
    }
}
