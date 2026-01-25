package com.fashion.order.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.fashion.order.common.annotation.ApiMessageResponse;
import com.fashion.order.dto.request.CouponRequest;
import com.fashion.order.dto.request.search.SearchRequest;
import com.fashion.order.dto.response.CouponResponse;
import com.fashion.order.dto.response.PaginationResponse;
import com.fashion.order.service.CouponService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponController {
    CouponService couponService;

    @PostMapping("")
    @ApiMessageResponse("coupon.success.create")
    public ResponseEntity<CouponResponse> createCoupon(
        @RequestBody @Validated(CouponRequest.Create.class) CouponRequest coupon
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.couponService.createCoupon(coupon));
    }

    @PutMapping("")
    @ApiMessageResponse("coupon.success.update")
    public ResponseEntity<CouponResponse> updateCoupon(
        @RequestBody @Validated(CouponRequest.Update.class) CouponRequest coupon
    ) {        
        return ResponseEntity.status(HttpStatus.OK).body(this.couponService.updateCoupon(coupon));
    }
    
    @GetMapping("/{id}")
    @ApiMessageResponse("coupon.success.get.single")
    public ResponseEntity<CouponResponse> getCouponById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(this.couponService.getCouponById(id));
    }

    @GetMapping("")
    @ApiMessageResponse("coupon.success.get.all")
    public ResponseEntity<PaginationResponse<List<CouponResponse>>> getAllCoupon(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.couponService.getAllCoupons(request));
    }
    
    @DeleteMapping("/{id}")
    @ApiMessageResponse("coupon.success.delete")
    public ResponseEntity<Void> deleteCouponById(
        @PathVariable("id") UUID id
    ){
        return ResponseEntity.noContent().build();
    }
}
