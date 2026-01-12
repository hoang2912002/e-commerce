package com.fashion.product.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
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
public class AddressRequest {
    public interface Create {}
    public interface Update {}

    UUID id;
    String address;
    String province;
    String district;
    String ward;
    Boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    Boolean currentUserAddress;
    UUID shopManagementId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InnerAddressRequest{
        // @NotNull(message = "", groups = Update.class)
        UUID id;

        @NotBlank(message = "address.address.notNull", groups = {ShopManagementRequest.Create.class, ShopManagementRequest.Update.class})
        String address;

        @NotBlank(message = "address.province.notNull", groups = {ShopManagementRequest.Create.class, ShopManagementRequest.Update.class})
        String province;

        @NotBlank(message = "address.district.notNull", groups = {ShopManagementRequest.Create.class, ShopManagementRequest.Update.class})
        String district;

        @NotBlank(message = "address.ward.notNull", groups = {ShopManagementRequest.Create.class, ShopManagementRequest.Update.class})
        String ward;
    }
}
