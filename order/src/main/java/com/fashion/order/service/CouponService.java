package com.fashion.order.service;

import java.util.List;
import java.util.UUID;

import com.fashion.order.dto.request.CouponRequest;
import com.fashion.order.dto.request.search.SearchRequest;
import com.fashion.order.dto.response.CouponResponse;
import com.fashion.order.dto.response.PaginationResponse;
import com.fashion.order.entity.Coupon;

public interface CouponService {
    CouponResponse createCoupon(CouponRequest coupon);
    CouponResponse updateCoupon(CouponRequest coupon);
    CouponResponse getCouponById(UUID id);
    PaginationResponse<List<CouponResponse>> getAllCoupons(SearchRequest request);
    void deleteCouponById(UUID id);
    Coupon validateCouponOrder(UUID id);
    void decreaseStock(UUID id); // Trừ số lượng
    void increaseStock(UUID id); // Hồi số lượng
}
