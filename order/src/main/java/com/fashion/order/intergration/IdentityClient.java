package com.fashion.order.intergration;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashion.order.common.response.ApiResponse;
import com.fashion.order.config.AuthenticationRequestInterceptor;
import com.fashion.order.dto.response.internal.AddressResponse;
import com.fashion.order.dto.response.internal.UserResponse;
import com.fashion.order.intergration.config.FeignClientConfigError;

@FeignClient(url = "${service.identity.url}", name = "${service.identity.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface IdentityClient {
    //--------------------Users--------------------
    @GetMapping(value = "/users/internal/get-internal-user-by-id")
    ApiResponse<UserResponse> getInternalUserById(
        @RequestParam("id") UUID id, 
        @RequestParam("version") Long version
    );

    //--------------------Address--------------------
    @GetMapping(value = "/addresses/internal/get-internal-address-by-id")
    ApiResponse<AddressResponse> getInternalAddressById(@RequestParam UUID id);
}
