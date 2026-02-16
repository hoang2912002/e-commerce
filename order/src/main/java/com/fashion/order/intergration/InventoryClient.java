package com.fashion.order.intergration;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.order.common.response.ApiResponse;
import com.fashion.order.config.AuthenticationRequestInterceptor;
import com.fashion.order.dto.request.OrderDetailRequest.InnerOrderDetail_FromOrderRequest;
import com.fashion.order.dto.response.internal.ProductResponse;
import com.fashion.order.intergration.config.FeignClientConfigError;

@FeignClient(url = "${service.inventory.url}", name = "${service.inventory.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface InventoryClient {
    //--------------------Products--------------------
    @PostMapping(value = "/inventories/internal/validate-internal-inventory-and-check-quantity-available")
    ApiResponse<Void> checkQuantityAvailableInventoryByProductSkuId(@RequestBody Collection<InnerOrderDetail_FromOrderRequest> inventory, @RequestParam Long version);
}
