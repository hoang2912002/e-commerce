package com.fashion.payment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.payment.entity.PaymentTransaction;
import com.fashion.payment.entity.PaymentTransactionId;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction,PaymentTransactionId>, JpaSpecificationExecutor<PaymentTransaction>{
    Boolean existsByEventId(UUID eventId);
}
