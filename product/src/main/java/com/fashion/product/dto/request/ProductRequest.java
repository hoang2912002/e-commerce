package com.fashion.product.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.request.CategoryRequest.InnerCategoryRequest;
import com.fashion.product.dto.request.ShopManagementRequest.InnerShopManagementRequest;
import com.fashion.product.dto.request.VariantRequest.InnerVariantRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class ProductRequest {
    public interface Create{};
    public interface Update{};

    @NotNull(message = "product.id.notNull", groups = {Update.class})
    UUID id;

    @NotBlank(message = "product.name.notNull", groups = {Create.class, Update.class})
    String name;
    
    @NotNull(message = "product.price.notNull", groups = {Create.class, Update.class})
    BigDecimal price;

    @NotNull(message = "server.version.not.be.null", groups = {Create.class, Update.class})
    Long version;
    
    String thumbnail;

    Integer quantity;
    
    String description;
    
    @Valid
    InnerCategoryRequest category;

    @Valid
    InnerShopManagementRequest shopManagement;
    
    @Valid
    List<InnerVariantRequest> variants;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerProductRequest {
        UUID id;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerInternalProductRequest {
        List<UUID> productIdList;
        List<UUID> productSkuIdList;
        
    }
}
