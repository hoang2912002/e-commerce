package com.fashion.payment.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.fashion.payment.common.annotation.ApiMessageResponse;
import com.fashion.payment.dto.request.PaymentMethodRequest;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentMethodResponse;
import com.fashion.payment.service.PaymentMethodService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/paymentMethods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentMethodController {
    PaymentMethodService paymentMethodService;

    @PostMapping("")
    @ApiMessageResponse("payment.method.success.create")
    public ResponseEntity<PaymentMethodResponse> createPaymentMethod(
        @RequestBody @Validated(PaymentMethodRequest.Create.class) PaymentMethodRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            this.paymentMethodService.createPaymentMethod(
                request
            )
        );
    } 
    
    @PutMapping("")
    @ApiMessageResponse("payment.method.success.update")
    public ResponseEntity<PaymentMethodResponse> updatePaymentMethod(
        @RequestBody @Validated(PaymentMethodRequest.Update.class) PaymentMethodRequest request
    ) {
        return ResponseEntity.ok(
            this.paymentMethodService.updatePaymentMethod(
                request
            )
        );
    } 
    
    @GetMapping("/{id}")
    @ApiMessageResponse("payment.method.success.get.single")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethodById(
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(this.paymentMethodService.getPaymentMethodById(id));
    } 
    
    @GetMapping("")
    @ApiMessageResponse("payment.method.success.get.all")
    public ResponseEntity<PaginationResponse<List<PaymentMethodResponse>>> getAllPaymentMethod(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.paymentMethodService.getAllPaymentMethod(request));
    } 

    @DeleteMapping("/{id}")
    @ApiMessageResponse("payment.method.success.delete")
    public ResponseEntity<Void> deletePaymentMethodById(
        @PathVariable("id") Long id
    ) {
        this.paymentMethodService.deletePaymentMethodById(id);
        return ResponseEntity.noContent().build();
    } 

}