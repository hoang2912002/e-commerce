package com.fashion.shipping.integration;

import java.time.Instant;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.shipping.common.response.ApiResponse;
import com.fashion.shipping.config.AuthenticationRequestInterceptor;
import com.fashion.shipping.dto.response.internal.OrderResponse;
import com.fashion.shipping.integration.config.FeignClientConfigError;

import jakarta.persistence.criteria.CriteriaBuilder.In;

@FeignClient(url = "${service.order.url}", name = "${service.order.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface OrderClient {
    //--------------------Products--------------------
    @GetMapping(value = "/orders/internal/get-internal-order-and-check-approval-by-id")
    ApiResponse<OrderResponse> getInternalOrderById(
        @RequestParam UUID orderId, 
        @RequestParam Long version,
        @RequestParam String orderCode
    );

    
}
