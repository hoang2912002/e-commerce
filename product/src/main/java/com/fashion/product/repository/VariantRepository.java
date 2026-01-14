package com.fashion.product.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.product.entity.Variant;


public interface VariantRepository extends JpaRepository<Variant, Long>, JpaSpecificationExecutor<Variant>{
    void deleteAllByProductId(UUID productId);
}
