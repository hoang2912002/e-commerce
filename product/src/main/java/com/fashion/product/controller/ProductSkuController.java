package com.fashion.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.common.annotation.InternalEndpoint;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.dto.response.ProductSkuResponse;
import com.fashion.product.service.ProductService;
import com.fashion.product.service.ProductSkuService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Retry(name = "order-service", fallbackMethod = "resilience4jRetryFallback")
@CircuitBreaker(name = "order-service", fallbackMethod = "resilience4jCircuitBreakerFallback")
@RateLimiter(name = "order-service", fallbackMethod = "resilience4jRateLimiterFallback")
@RestController
@RequestMapping("/productSkus")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductSkuController extends R4jFallback {
    ProductSkuService productSkuService;

    //------------------Internal endpoint-----------------------
    @InternalEndpoint
    @GetMapping("/internal/get-internal-product-sku-by-list-id")
    @ApiMessageResponse("product.success.internal.get.single")
    public ResponseEntity<List<ProductSkuResponse>> getInternalProductSkuByIds(
        @RequestParam List<UUID> productSkuIds
    ) {
        return ResponseEntity.ok(this.productSkuService.getInternalProductSkuByIds(productSkuIds));
    }
}
