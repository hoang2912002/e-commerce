package com.fashion.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.payment.entity.Payment;
import com.fashion.payment.entity.PaymentMethod;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface PaymentRepository extends JpaRepository<Payment,UUID>, JpaSpecificationExecutor<Payment>{
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId")
    Optional<Payment> findDuplicateForCreate(
        @Param("orderId") UUID orderId
    );
    
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.id != :i")
    Optional<Payment> findDuplicateForUpdate(
        @Param("orderId") UUID orderId,
        @Param("i") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<Payment> lockPaymentById(@Param("id") UUID id);
}
