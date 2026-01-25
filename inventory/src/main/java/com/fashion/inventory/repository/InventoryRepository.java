package com.fashion.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.inventory.entity.Inventory;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;


public interface InventoryRepository extends JpaRepository<Inventory, UUID>, JpaSpecificationExecutor<Inventory> {
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.productSkuId = :productSkuId AND i.wareHouse.id = :wareHouseId")
    Optional<Inventory> findDuplicateForCreate(
        @Param("productId") UUID productId,
        @Param("productSkuId") UUID productSkuId,
        @Param("wareHouseId") UUID wareHouseId
    );
    
    @Query("SELECT i FROM Inventory i WHERE (i.productId = :productId AND i.productSkuId = :productSkuId AND i.wareHouse.id = :wareHouseId) AND i.id != :id")
    Optional<Inventory> findDuplicateForUpdate(
        @Param("productId") UUID productId,
        @Param("productSkuId") UUID productSkuId,
        @Param("wareHouseId") UUID wareHouseId,
        @Param("id") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productSkuId IN :skuIds")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0") // 0 = fail immediately
    })
    List<Inventory> lockInventoryBySkuId(@Param("skuIds") List<UUID> skuIds);

    @EntityGraph(attributePaths = "wareHouse")
    List<Inventory> findAllByProductSkuIdIn(Set<UUID> productSkuId);
}
