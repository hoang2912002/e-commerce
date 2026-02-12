package com.fashion.inventory.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.fashion.inventory.common.enums.WareHouseStatusEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class WareHouseRequest {
    public interface Create {}
    public interface Update {}
    public interface UpdateStatus {}

    @NotNull(groups = {UpdateStatus.class, Update.class}, message = "ware.house.id.notNull")
    UUID id;
    @NotBlank(groups = {Create.class, Update.class}, message = "ware.house.code.notNull")
    @Pattern(
        groups = {Create.class, Update.class},
        regexp = "^[A-Z0-9_-]{3,20}$", 
        message = "ware.house.code.format.invalid"
    )
    String code;
    @NotBlank(groups = {Create.class, Update.class}, message = "ware.house.name.notNull")
    String name;
    @NotBlank(groups = {Create.class, Update.class}, message = "ware.house.location.notNull")
    String location;

    @NotNull(groups = {UpdateStatus.class}, message = "ware.house.status.notNull")
    WareHouseStatusEnum status;

    Boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;

    @NotNull(groups = {Create.class, Update.class}, message = "ware.house.version.notNull")
    Long version;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerWareHouseRequest {
        @NotNull(groups = {InventoryRequest.Create.class,InventoryRequest.Update.class}, message = "ware.house.id.notNull")
        UUID id;
    }
}
