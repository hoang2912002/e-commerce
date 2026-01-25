package com.fashion.order.intergration;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.order.common.response.ApiResponse;
import com.fashion.order.config.AuthenticationRequestInterceptor;
import com.fashion.order.dto.request.internal.ProductRequest.InnerInternalProductRequest;
import com.fashion.order.dto.response.internal.ProductResponse;
import com.fashion.order.dto.response.internal.UserResponse;
import com.fashion.order.intergration.config.FeignClientConfigError;

@FeignClient(url = "${service.product.url}", name = "${service.product.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface ProductClient {
    //--------------------Products--------------------
    @PostMapping(value = "/products/internal/get-internal-product-and-product-sku-and-check-approval-by-id")
    ApiResponse<List<ProductResponse>> getInternalProductAndProductSkuById(@RequestBody InnerInternalProductRequest request);

    
}
