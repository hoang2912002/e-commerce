package com.fashion.inventory.dto.request;

import java.time.Instant;
import java.util.UUID;

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

    @NotNull(groups = Update.class, message = "ware.house.id.notNull")
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
    Boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerWareHouseRequest {
        UUID id;
        String code;
        String name;
        String location;
    }
}
