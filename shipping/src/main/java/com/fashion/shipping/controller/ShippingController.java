package com.fashion.shipping.controller;

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

import com.fashion.shipping.common.annotation.ApiMessageResponse;
import com.fashion.shipping.dto.request.ShippingRequest;
import com.fashion.shipping.dto.request.search.SearchRequest;
import com.fashion.shipping.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.shipping.dto.response.PaginationResponse;
import com.fashion.shipping.dto.response.ShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerInternalShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerTempShippingFeeResponse;
import com.fashion.shipping.dto.response.internal.PaymentResponse;
import com.fashion.shipping.service.ShippingService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/shippings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShippingController {
    ShippingService shippingService;
    
    @GetMapping("/{id}")
    @ApiMessageResponse("shipping.success.get.single")
    public ResponseEntity<ShippingResponse> getShippingById(
        @PathVariable("id") UUID id,
        @RequestParam("date") String date,
        @RequestParam("version") Long version
    ) {
        return ResponseEntity.ok(this.shippingService.getShippingById(id, date, version));
    } 

    @PostMapping("")
    public ResponseEntity<ShippingResponse> createShipping(
        @RequestBody @Validated(ShippingRequest.Create.class) ShippingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.shippingService.createShipping(request));
    }

    @PutMapping("")
    public ResponseEntity<ShippingResponse> updateShipping(
        @RequestBody @Validated(ShippingRequest.Update.class) ShippingRequest request
    ) {
        return ResponseEntity.ok(this.shippingService.updateShipping(request));
    }
    
    
    @GetMapping("")
    @ApiMessageResponse("shipping.success.get.all")
    public ResponseEntity<PaginationResponse<List<ShippingResponse>>> getAllShipping(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.shippingService.getAllShipping(request));
    } 

    // ----------------internal-----------------------
    @PostMapping("/internal/get-internal-information-third-party-shipping")
    @ApiMessageResponse("shipping.success.get.single")
    public ResponseEntity<InnerTempShippingFeeResponse> getInternalThirdPartyShippingFree(
        @RequestBody InnerInternalShippingResponse request
    ) {
        return ResponseEntity.ok(this.shippingService.getThirdPartyShippingFee(request));
    }
    
}
