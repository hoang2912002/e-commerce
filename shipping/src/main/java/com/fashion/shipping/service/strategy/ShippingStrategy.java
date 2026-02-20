package com.fashion.shipping.service.strategy;

import java.util.UUID;

import com.fashion.shipping.dto.response.ShippingResponse.InnerInternalShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerTempShippingFeeResponse;

public interface ShippingStrategy {
    InnerTempShippingFeeResponse shippingFee(InnerInternalShippingResponse addressShipping);
    <I,O> O createOrder(I request);
    String getProviderName();
    ThirdPartyAddressKey cacheThirdPartyAddressKey();
    ThirdPartyAddressKey lockThirdPartyAddressKey();
    void warmAddressCache();
}
