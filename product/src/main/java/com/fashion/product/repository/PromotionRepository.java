package com.fashion.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.product.entity.Promotion;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface PromotionRepository extends JpaRepository<Promotion, UUID>, JpaSpecificationExecutor<Promotion>{
    @Query("SELECT p FROM Promotion p WHERE p.code = :c")
    Optional<Promotion> findDuplicateForCreate(
        @Param("c") String code
    );
    
    @Query("SELECT p FROM Promotion p WHERE p.code = :c and p.id != :i")
    Optional<Promotion> findDuplicateForUpdate(
        @Param("c") String code,
        @Param("i") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE p.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Promotion lockPromotionById(@Param("id") UUID id);


    @Modifying
    @Query(value = "UPDATE promotions " +
        "SET quantity = quantity - :quantity " +
        "WHERE id = :id " +
        "AND quantity >= :quantity", 
    nativeQuery = true)
    int decreaseQuantityAtomic(
        @Param("id") UUID id,
        @Param("quantity") Integer quantity
    );
    
    @Modifying
    @Query(value = "UPDATE promotions " +
        "SET quantity = quantity + :quantity " +
        "WHERE id = :id ", 
    nativeQuery = true)
    int increaseQuantityAtomic(
        @Param("id") UUID id,
        @Param("quantity") Integer quantity
    );
}
