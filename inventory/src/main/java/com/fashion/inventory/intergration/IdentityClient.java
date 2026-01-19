package com.fashion.inventory.intergration;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fashion.inventory.common.response.ApiResponse;
import com.fashion.inventory.config.AuthenticationRequestInterceptor;
import com.fashion.inventory.dto.response.AddressResponse;
import com.fashion.inventory.dto.response.RoleResponse;
import com.fashion.inventory.dto.response.UserResponse;
import com.fashion.inventory.intergration.config.FeignClientConfigError;

@FeignClient(url = "${service.identity.url}", name = "${service.identity.name}", configuration = {
    AuthenticationRequestInterceptor.class,
    FeignClientConfigError.class
})
public interface IdentityClient {
    //--------------------Users--------------------
    @GetMapping(value = "/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") UUID id);

    //--------------------Address--------------------
    @GetMapping(value = "/addresses/{id}")
    ApiResponse<AddressResponse> getAddressById(@PathVariable("id") UUID id);

    //--------------------Role--------------------
    @GetMapping(value = "/roles/{id}")
    ApiResponse<RoleResponse> getRoleById(@PathVariable("id") Long id);

}
