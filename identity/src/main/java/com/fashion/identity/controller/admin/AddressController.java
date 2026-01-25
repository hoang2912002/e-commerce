package com.fashion.identity.controller.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.identity.common.annotation.ApiMessageResponse;
import com.fashion.identity.common.annotation.InternalEndpoint;
import com.fashion.identity.dto.request.AddressRequest;
import com.fashion.identity.dto.request.UserRequest;
import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.AddressResponse;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.entity.User;
import com.fashion.identity.mapper.AddressMapper;
import com.fashion.identity.service.AddressService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressController {
    AddressService addressService;
    AddressMapper addressMapper;

    @GetMapping("")
    @ApiMessageResponse("address.success.get.all")
    public ResponseEntity<PaginationResponse<List<AddressResponse>>> getAllAddresses(
        @ModelAttribute UserSearchRequest request
    ) {
        return ResponseEntity.ok(this.addressService.getAllAddresses(request));
    }


    @PostMapping("")
    @ApiMessageResponse("address.success.create")
    public ResponseEntity<AddressResponse> createAddress(
        @RequestBody @Validated(AddressRequest.Create.class) AddressRequest address
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.addressService.createAddress(addressMapper.toValidated(address)));
    }

    @PutMapping("")
    @ApiMessageResponse("address.success.update")
    public ResponseEntity<AddressResponse> updateAddress(
        @RequestBody @Validated(AddressRequest.Update.class) AddressRequest address
    ) {
        return ResponseEntity.ok(this.addressService.updateAddress(addressMapper.toValidated(address)));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("address.success.get.single")
    public ResponseEntity<AddressResponse> getAddressById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(this.addressService.getAddressById(id));
    }

    @DeleteMapping("/{id}")
    @ApiMessageResponse("address.success.delete")
    public void deleteAddressById(
        @PathVariable("id") UUID id
    ){
        this.addressService.deleteAddressById(id);
    }

    //Internal Endpoint
    @InternalEndpoint
    @GetMapping("/internal/get-internal-address-by-id")
    @ApiMessageResponse("user.success.get.single")
    public ResponseEntity<AddressResponse> getInternalAddressById(
        @RequestParam UUID id
    ) {
        return ResponseEntity.ok(this.addressService.getAddressById(id));
    }
}
