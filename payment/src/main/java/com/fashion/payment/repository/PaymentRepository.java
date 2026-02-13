package com.fashion.payment.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.payment.entity.Payment;
import com.fashion.payment.entity.PaymentId;
import com.fashion.payment.entity.PaymentMethod;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface PaymentRepository extends JpaRepository<Payment,PaymentId>, JpaSpecificationExecutor<Payment>{
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId " +
           "AND p.createdAt >= :start AND p.createdAt < :end")
    Optional<Payment> findDuplicateForCreate(
        @Param("orderId") UUID orderId,
        @Param("start") Instant start,
        @Param("end") Instant end
    );
    
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.id != :i " +
           "AND p.createdAt >= :start AND p.createdAt < :end")
    Optional<Payment> findDuplicateForUpdate(
        @Param("orderId") UUID orderId,
        @Param("i") UUID id,
        @Param("start") Instant start,
        @Param("end") Instant end
    );

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id " +
           "AND p.createdAt >= :start AND p.createdAt < :end")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<Payment> lockPaymentById(
        @Param("id") UUID id,
        @Param("start") Instant start,
        @Param("end") Instant end
    );

    @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.createdAt >= :start AND p.createdAt < :end")
    Optional<Payment> findByIdInPartition(
        @Param("id") UUID id, 
        @Param("start") Instant start, 
        @Param("end") Instant end
    );
}
