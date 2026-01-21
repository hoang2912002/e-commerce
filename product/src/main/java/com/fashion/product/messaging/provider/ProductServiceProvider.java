package com.fashion.product.messaging.provider;

import java.util.List;

import com.fashion.product.dto.response.kafka.ProductApprovedEvent;
import com.fashion.product.dto.response.kafka.ProductApprovedEvent.InternalProductApprovedEvent;
import com.fashion.product.dto.response.kafka.ShopManagementAddressEvent;

public interface ProductServiceProvider {
    public void produceShopManagementEventSuccess(ShopManagementAddressEvent addressData);
    public void produceProductApprovedEventSuccess(InternalProductApprovedEvent event);
}
