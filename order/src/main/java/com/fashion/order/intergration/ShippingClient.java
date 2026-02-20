package com.fashion.order.intergration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.order.common.response.ApiResponse;
import com.fashion.order.config.AuthenticationRequestInterceptor;
import com.fashion.order.dto.response.internal.ShippingResponse.InnerInternalShippingResponse;
import com.fashion.order.dto.response.internal.ShippingResponse.InnerTempShippingFeeResponse;
import com.fashion.order.dto.response.internal.UserResponse;
import com.fashion.order.intergration.config.FeignClientConfigError;

@FeignClient(url = "${service.shipping.url}", name = "${service.shipping.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface ShippingClient {
    //--------------------Shippings--------------------
    @PostMapping(value = "/shippings/internal/get-internal-information-third-party-shipping")
    ApiResponse<InnerTempShippingFeeResponse> getInternalShippingFeeThirdParty(@RequestBody InnerInternalShippingResponse innerInternalShippingResponse);
}
