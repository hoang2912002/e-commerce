package com.fashion.inventory.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.fashion.inventory.dto.request.WareHouseRequest.InnerWareHouseRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class InventoryRequest {
    public interface Create {}
    public interface Update {}

    @NotNull(groups = Update.class, message = "inventory.id.notNull")
    UUID id;
    @NotNull(groups = {Create.class,Update.class}, message = "inventory.quantityAvailable.notNull")
    @Min(value = 1, groups = {Create.class,Update.class}, message = "inventory.quantityAvailable.invalid")
    Integer quantityAvailable;
    @NotNull(groups = {Create.class,Update.class}, message = "inventory.quantityReserved.notNull")
    Integer quantityReserved;
    @NotNull(groups = {Create.class,Update.class}, message = "inventory.quantitySold.notNull")
    Integer quantitySold;
    @NotNull(groups = {Create.class,Update.class}, message = "inventory.productId.notNull")
    UUID productId;
    @NotNull(groups = {Create.class,Update.class}, message = "inventory.productSkuId.notNull")
    UUID productSkuId;
    @Valid
    InnerWareHouseRequest warehouse;
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
    public static class InnerInventoryRequest {
        UUID id;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BaseInventoryRequest {
        UUID id;
        UUID productSkuId;
        Integer quantity;
        String referenceType;
        Long referenceId;
        String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnAvailableQuantity {
        UUID productId;
        UUID productSkuId;
        // Số lượng luân chuyển
        Integer circulationCount;
        // sl giữa mới > sl giữ cũ
        boolean isNegative;
    }
}
