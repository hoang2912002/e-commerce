package com.fashion.identity.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
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
    String shopManagementId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InnerAddressRequest{
        UUID id;

        @NotBlank(message = "address.address.notNull")
        String address;

        @NotBlank(message = "address.province.notNull")
        String province;

        @NotBlank(message = "address.district.notNull")
        String district;

        @NotBlank(message = "address.ward.notNull")
        String ward;
    }
}
