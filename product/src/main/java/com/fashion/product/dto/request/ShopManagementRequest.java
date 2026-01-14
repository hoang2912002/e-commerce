package com.fashion.product.dto.request;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fashion.product.dto.request.AddressRequest.InnerAddressRequest;
import com.fashion.product.dto.request.UserRequest.InnerUserRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    public interface Create{};
    public interface Update{};

    @NotNull(message = "shop.management.id.notNull", groups = {Update.class})
    UUID id;
    String slug;

    @NotBlank(message = "shop.management.name.notNull", groups = {Create.class, Update.class})
    String name;
    @NotBlank(message = "shop.management.businessName.notNull", groups = {Create.class, Update.class})
    String businessName;
    @NotBlank(message = "shop.management.businessNo.notNull", groups = {Create.class, Update.class})
    String businessNo;
    LocalDate businessDateIssue;

    @NotBlank(message = "shop.management.businessPlace.notNull", groups = {Create.class, Update.class})
    String businessPlace;

    @NotBlank(message = "shop.management.taxCode.notNull", groups = {Create.class, Update.class})
    String taxCode;

    Integer businessType;

    @NotBlank(message = "shop.management.accountName.notNull", groups = {Create.class, Update.class})
    String accountName;

    @NotBlank(message = "shop.management.accountNumber.notNull", groups = {Create.class, Update.class})
    String accountNumber;
    
    @NotBlank(message = "shop.management.bankName.notNull", groups = {Create.class, Update.class})
    String bankName;

    @NotBlank(message = "shop.management.bankBranch.notNull", groups = {Create.class, Update.class})
    String bankBranch;
    // String logo;
    // String thumbnail;
    // String businessLicence;
    // String identificationImageFirst;
    // String identificationImageSecond;
    String description;
    
    @Valid
    InnerAddressRequest address;
    @Valid
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
        @NotNull(message = "category.id.notNull", groups = { 
            ProductRequest.Create.class,
            ProductRequest.Update.class,
        })
        UUID id;
    }
}
