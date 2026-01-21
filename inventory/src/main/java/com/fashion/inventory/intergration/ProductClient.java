package com.fashion.inventory.intergration;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.inventory.common.response.ApiResponse;
import com.fashion.inventory.config.AuthenticationRequestInterceptor;
import com.fashion.inventory.dto.response.internal.ApprovalHistoryResponse;
import com.fashion.inventory.dto.response.internal.ProductResponse;
import com.fashion.inventory.dto.response.internal.UserResponse;
import com.fashion.inventory.intergration.config.FeignClientConfigError;

@FeignClient(url = "${service.product.url}", name = "${service.product.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface ProductClient {
    //--------------------Product--------------------
    @GetMapping(value = "/products/internal/validate-internal-product-product-sku")
    ApiResponse<Void> validateInternalProductById(@RequestParam UUID productId, @RequestParam UUID productSkuId);
    
    //--------------------Approval history--------------------
    @GetMapping(value = "/approvalHistories/internal/validate-internal-approval-history-by-requestId")
    ApiResponse<Void> validateInternalApprovalHistoryByRequestId(@RequestParam UUID requestId);
}
