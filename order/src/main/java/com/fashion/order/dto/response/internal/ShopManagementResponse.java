package com.fashion.order.dto.response.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fashion.order.dto.response.internal.AddressResponse.InnerAddressResponse;
import com.fashion.order.dto.response.internal.UserResponse.InnerUserResponse;

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
public class ShopManagementResponse {
    UUID id;
    String slug;
    String name; // Tên gian hàng
    String businessName; // Tên công ty
    String businessNo; // Mã cty
    LocalDate businessDateIssue; // Ngày thành lập
    String businessPlace; // Địa chỉ cty
    String taxCode; // Mã thuế
    Integer businessType;
    String accountName;
    String accountNumber;
    String bankName;
    String bankBranch;
    String logo;
    String thumbnail;
    String businessLicence;
    String identificationImageFirst;
    String identificationImageSecond;
    String description;
    
    InnerAddressResponse address;
    InnerUserResponse user;

    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updateAt;
    Boolean activated;


    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerShopManagementResponse {
        UUID id;
        String name;
        String slug;
    }
}
