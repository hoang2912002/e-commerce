package com.fashion.payment.controller;

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

import com.fashion.payment.common.annotation.ApiMessageResponse;
import com.fashion.payment.dto.request.PaymentMethodRequest;
import com.fashion.payment.dto.request.PaymentRequest;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.service.PaymentService;

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
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController extends R4jFallback {
    PaymentService paymentService;

    @PostMapping("")
    @ApiMessageResponse("payment.success.create")
    public ResponseEntity<PaymentResponse> createPayment(
        @RequestBody @Validated(PaymentMethodRequest.Create.class) PaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            this.paymentService.createPayment(
                request
            )
        );
    } 
    
    @PutMapping("")
    @ApiMessageResponse("payment.success.update")
    public ResponseEntity<PaymentResponse> updatePayment(
        @RequestBody @Validated(PaymentMethodRequest.Update.class) PaymentRequest request
    ) {
        return ResponseEntity.ok(
            this.paymentService.updatePayment(
                request
            )
        );
    } 
    
    @GetMapping("/{id}")
    @ApiMessageResponse("payment.success.get.single")
    public ResponseEntity<PaymentResponse> getPaymentById(
        @PathVariable("id") UUID id,
        @RequestParam("date") String date,
        @RequestParam("version") Long version
    ) {
        return ResponseEntity.ok(this.paymentService.getPaymentById(id, date, version));
    } 
    
    @GetMapping("")
    @ApiMessageResponse("payment.success.get.all")
    public ResponseEntity<PaginationResponse<List<PaymentResponse>>> getAllPayment(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.paymentService.getAllPayment(request));
    } 

    @DeleteMapping("/{id}")
    @ApiMessageResponse("payment.success.delete")
    public ResponseEntity<Void> deletePaymentById(
        @PathVariable("id") UUID id
    ) {
        this.paymentService.deletePaymentById(id);
        return ResponseEntity.noContent().build();
    } 
}
