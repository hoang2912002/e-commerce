package com.fashion.product.dto.response;

import java.time.Instant;
import java.util.UUID;

import javax.swing.text.StyledEditorKit.BoldAction;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    UUID id;
    String address;
    String district;
    String province;
    String ward;
    UUID userId;
    Boolean currentUserAddress;
    String shopManagementId;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Boolean activated;
    Instant updatedAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerAddressResponse {
        UUID id;
        String address;
        String district;
        String province;
        String ward;
    }
}
