package com.fashion.product.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.entity.ProductSku;

import feign.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, UUID>, JpaSpecificationExecutor<ProductSku>{
    List<ProductSku> findAllByProductId(UUID productId);
    List<ProductSku> findAllByIdIn(Set<UUID> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProductSku s WHERE s.product.id = :productId")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    List<ProductSku> lockSkuByProduct(@Param("productId") UUID productId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductSku p SET p.tempStock = 0 WHERE p.id IN :ids")
    void updateTempStockToZero(@Param("ids") List<UUID> ids);
}
