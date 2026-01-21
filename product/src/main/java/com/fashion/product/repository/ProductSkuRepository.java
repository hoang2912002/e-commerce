package com.fashion.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.fashion.product.entity.ProductSku;

import feign.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ProductSkuRepository extends JpaRepository<ProductSku, UUID>, JpaSpecificationExecutor<ProductSku>{
    List<ProductSku> findAllByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProductSku s WHERE s.product.id = :productId")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    List<ProductSku> lockSkuByProduct(@Param("productId") UUID productId);
}
