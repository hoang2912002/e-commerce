package com.fashion.order.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.order.entity.Coupon;

import feign.Param;

@Repository
public interface CouponRepository extends JpaRepository<Coupon,UUID>, JpaSpecificationExecutor<Coupon> {
    Optional<Coupon> findByCode(String code);
    Optional<Coupon> findByCodeAndIdNot(String code, UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Coupon c SET c.stock = :newStock WHERE c.id = :id")
    int updateStockAtomic(@Param("id") UUID id, @Param("newStock") Integer newStock);

    List<Coupon> findTop100ByOrderByCreatedAtDesc();

}
