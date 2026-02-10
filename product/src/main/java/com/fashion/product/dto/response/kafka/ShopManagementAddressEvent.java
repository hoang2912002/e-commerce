package com.fashion.product.dto.response.kafka;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopManagementAddressEvent {
    UUID id;
    UUID shopManagementId;
    String address;
    String district;
    String province;
    String ward;


    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InternalShopManagementAddressEvent extends ApplicationEvent {
        ShopManagementAddressEvent shopManagementAddressEvent;

        public InternalShopManagementAddressEvent(Object source, ShopManagementAddressEvent shopManagementAddressEvent) {
            super(source);
            this.shopManagementAddressEvent = shopManagementAddressEvent;
        }
    }
}
