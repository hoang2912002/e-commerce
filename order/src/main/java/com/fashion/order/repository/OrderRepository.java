package com.fashion.order.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.order.common.enums.OrderEnum;
import com.fashion.order.common.enums.PaymentEnum;
import com.fashion.order.common.enums.ShippingEnum;
import com.fashion.order.entity.Order;
import com.fashion.order.entity.OrderId;

import feign.Param;

public interface OrderRepository extends JpaRepository<Order,OrderId>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByCode(String code);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.paymentId = :paymentId, o.paymentStatus =:paymentStatus WHERE o.id = :id AND o.createdAt = :createdAt")
    void updatePaymentId(@Param("id") UUID id, @Param("createdAt") Instant createdAt, @Param("paymentId") UUID paymentId, @Param("paymentStatus") PaymentEnum paymentStatus);
    
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.shippingId = :shippingId, o.shippingStatus =:shippingStatus WHERE o.id = :id AND o.createdAt = :createdAt")
    void updateShippingId(@Param("id") UUID id, @Param("createdAt") Instant createdAt, @Param("shippingId") UUID shippingId, @Param("shippingStatus") ShippingEnum shippingStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id AND o.createdAt = :createdAt")
    void updateAtomicStatus(@Param("id") UUID id, @Param("createdAt") Instant createdAt, @Param("status") OrderEnum status);

    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.createdAt >= :start AND o.createdAt < :end")
    Optional<Order> findByIdInPartition(
        @Param("id") UUID id,
        @Param("start") Instant start,
        @Param("end") Instant end 
    );
}
