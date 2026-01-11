package com.fashion.product.dto.request;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fashion.product.dto.request.UserRequest.InnerUserRequest;

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
public class ShopManagementRequest {
    UUID id;
    String slug;
    String name;
    String businessName;
    String businessNo;
    LocalDate businessDateIssue;
    String businessPlace;
    String taxCode;
    Integer businessType;
    String accountName;
    String accountNumber;
    String bankName;
    String bankBranch;
    // String logo;
    // String thumbnail;
    // String businessLicence;
    // String identificationImageFirst;
    // String identificationImageSecond;
    String description;
    
    String address;
    InnerUserRequest user;

    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updateAt;
    Boolean activated;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerShopManagementRequest {
        Long id;
    }
}
