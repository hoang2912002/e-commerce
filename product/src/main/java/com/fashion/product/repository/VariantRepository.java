package com.fashion.product.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.entity.Variant;

import feign.Param;


public interface VariantRepository extends JpaRepository<Variant, Long>, JpaSpecificationExecutor<Variant>{
    @Modifying
    @Query(value = "DELETE FROM variants WHERE product_id = :productId", nativeQuery = true)
    void deleteAllByProductId(@Param("productId") UUID productId);
}
