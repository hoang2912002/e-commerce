package com.fashion.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.payment.entity.PaymentMethod;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;


public interface PaymentMethodRepository extends JpaRepository<PaymentMethod,Long>, JpaSpecificationExecutor<PaymentMethod>{
    @Query("SELECT p FROM PaymentMethod p WHERE p.code = :code AND p.name =:name")
    Optional<PaymentMethod> findDuplicateForCreate(
        @Param("code") String code,
        @Param("name") String name
    );
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.code = :code AND p.name =:name and p.id != :i")
    Optional<PaymentMethod> findDuplicateForUpdate(
        @Param("code") String code,
        @Param("name") String name,
        @Param("i") Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentMethod p WHERE p.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<PaymentMethod> lockPaymentMethodById(@Param("id") Long id);


    Optional<PaymentMethod> findByCodeOrId(String code, Long id);
}
