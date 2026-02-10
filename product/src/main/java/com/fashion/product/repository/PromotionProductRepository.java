package com.fashion.product.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.fashion.product.entity.Promotion;
import com.fashion.product.entity.PromotionProduct;

import feign.Param;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long>, JpaSpecificationExecutor<PromotionProduct>{
    // List<PromotionProduct> findAllByProductId(UUID productId);
    
    @Query("SELECT pp FROM PromotionProduct pp " +
           "JOIN FETCH pp.promotion p " +
           "WHERE pp.product.id =:productId " +
           "AND p.endDate >= :currentDate")
    List<PromotionProduct> findAllByProductId(
        @Param("productId") UUID productId,
        @Param("currentDate") LocalDate currentDate
    );

    // Overload without date parameter (uses current date)
    default List<PromotionProduct> findAllByProductId(UUID productId) {
        return findAllByProductId(productId, LocalDate.now());
    }
}
