package com.fashion.product.intergration;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fashion.product.common.response.ApiResponse;
import com.fashion.product.config.AuthenticationRequestInterceptor;
import com.fashion.product.dto.response.AddressResponse;
import com.fashion.product.dto.response.UserResponse;

@FeignClient(url = "${service.identity.url}", name = "${service.identity.name}", configuration = {
    AuthenticationRequestInterceptor.class
})
public interface IdentityClient {
    //--------------------Users--------------------
    @GetMapping(value = "/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") UUID id);

    //--------------------Address--------------------
    @GetMapping(value = "/addresses/{id}")
    ApiResponse<AddressResponse> getAddressById(@PathVariable("id") UUID id);

}
