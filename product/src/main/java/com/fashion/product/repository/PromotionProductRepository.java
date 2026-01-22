package com.fashion.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.product.entity.Promotion;
import com.fashion.product.entity.PromotionProduct;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long>, JpaSpecificationExecutor<PromotionProduct>{
    List<PromotionProduct> findAllByProductId(UUID productId);
}
