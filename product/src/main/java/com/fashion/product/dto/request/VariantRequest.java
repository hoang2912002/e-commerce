package com.fashion.product.dto.request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.request.OptionRequest.InnerOptionRequest;
import com.fashion.product.dto.request.OptionValueRequest.InnerOptionValueRequest;
import com.fashion.product.dto.request.ProductRequest.InnerProductRequest;
import com.fashion.product.dto.request.ProductSkuRequest.InnerProductSkuRequest;
import com.fashion.product.entity.OptionValue;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantRequest {
    UUID id;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    boolean activated;
    InnerOptionRequest option;
    InnerOptionValueRequest optionValue;
    InnerProductRequest product;
    InnerProductSkuRequest productSku;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerVariantRequest {
        String skuId;
        List<String> optionValues;
        Double price;
        Integer stock;
        // String thumbnail;
    }
}
