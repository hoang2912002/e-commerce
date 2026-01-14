package com.fashion.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.product.entity.ProductSku;

public interface ProductSkuRepository extends JpaRepository<ProductSku, UUID>, JpaSpecificationExecutor<ProductSku>{
    List<ProductSku> findAllByProductId(UUID productId);
}
