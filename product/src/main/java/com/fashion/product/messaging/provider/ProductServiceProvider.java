package com.fashion.product.messaging.provider;

import com.fashion.product.dto.response.kafka.ShopManagementAddressEvent;

public interface ProductServiceProvider {
    public void produceShopManagementEventSuccess(ShopManagementAddressEvent addressData);
}
