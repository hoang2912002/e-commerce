package com.fashion.payment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.payment.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction,Long>, JpaSpecificationExecutor<PaymentTransaction>{
    Boolean existsByEventId(UUID eventId);
}
