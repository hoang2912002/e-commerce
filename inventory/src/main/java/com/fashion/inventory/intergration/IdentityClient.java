package com.fashion.inventory.intergration;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.inventory.common.response.ApiResponse;
import com.fashion.inventory.config.AuthenticationRequestInterceptor;
import com.fashion.inventory.dto.response.internal.AddressResponse;
import com.fashion.inventory.dto.response.internal.RoleResponse;
import com.fashion.inventory.dto.response.internal.UserResponse;
import com.fashion.inventory.intergration.config.FeignClientConfigError;

@FeignClient(url = "${service.identity.url}", name = "${service.identity.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface IdentityClient {
    //--------------------Users--------------------
    @GetMapping(value = "/users/internal/validate-internal-user-role-by-id")
    ApiResponse<Void> validateInternalUserById(@RequestParam UUID id, @RequestParam Boolean isCheckRole, @RequestParam Long version);
}
