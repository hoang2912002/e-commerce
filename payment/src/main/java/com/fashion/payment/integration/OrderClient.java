package com.fashion.payment.integration;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.payment.common.response.ApiResponse;
import com.fashion.payment.config.AuthenticationRequestInterceptor;
import com.fashion.payment.dto.response.internal.OrderResponse;
import com.fashion.payment.integration.config.FeignClientConfigError;

@FeignClient(url = "${service.order.url}", name = "${service.order.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface OrderClient {
    //--------------------Products--------------------
    @GetMapping(value = "/orders/internal/get-internal-order-and-check-approval-by-id")
    ApiResponse<OrderResponse> getInternalOrderById(@RequestParam UUID orderId, @RequestParam Long version);

    
}
