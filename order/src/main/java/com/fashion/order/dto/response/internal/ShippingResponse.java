package com.fashion.order.dto.response.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.order.common.enums.ShippingEnum;
import com.fashion.order.common.enums.ShippingProvider;
import com.fashion.order.dto.response.VersionResponse;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ShippingResponse extends VersionResponse {

    public void shippingResponse(Long version){
        super.version = version;
    }

    UUID id;
    LocalDateTime deliveredAt;
    LocalDateTime estimatedDate;

    @Enumerated(EnumType.STRING)
    ShippingProvider provider;
    LocalDateTime shippingAt;
    BigDecimal shippingFee;

    @Enumerated(EnumType.STRING)
    ShippingEnum status;
    String trackingCode;
    UUID orderId;
    String orderCode;
    Instant orderCreatedAt;
    Boolean activated;
    UUID eventId;
    
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerShippingResponse {
        UUID id;   
        ShippingProvider provider;
        LocalDateTime shippingAt;
        BigDecimal shippingFee;    
        ShippingEnum status; 
        LocalDateTime deliveredAt;
        LocalDateTime estimatedDate;
    }

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerTempShippingFeeResponse extends VersionResponse{
        ShippingProvider provider;
        BigDecimal shippingFee;
        LocalDateTime estimatedDate;

        public void innerTempShippingFeeResponse(Long version){
            super.version = version;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerInternalShippingResponse {
        ShippingProvider provider;
        String address;
        String district;
        String province;
        String ward;
        Long version;
    }
}
