package com.fashion.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.order.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon,UUID>, JpaSpecificationExecutor<Coupon> {
    Optional<Coupon> findByCode(String code);
    Optional<Coupon> findByCodeAndIdNot(String code, UUID id);

}
