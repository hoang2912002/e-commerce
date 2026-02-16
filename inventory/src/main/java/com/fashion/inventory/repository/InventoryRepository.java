package com.fashion.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.inventory.entity.Inventory;
import com.fashion.inventory.entity.WareHouse;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
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

    Optional<Inventory> findByProductIdAndProductSkuId(UUID productId, UUID productSkuId);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.id =:id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<Inventory> lockInventoryById(@Param("id") UUID id);

    // @Modifying only use for command line return back number of row changed(Specially support for INSERT,UPDATE,DELETE)
    @Query(value = "UPDATE inventories " +
                "SET quantity_available = quantity_available - :quantity, " +
                "quantity_reserved = quantity_reserved + :quantity " +
                "WHERE product_id = :productId " +
                "AND product_sku_id = :productSkuId " +
                "AND quantity_available >= :quantity " +
                "RETURNING *", // Professional way in PostgreSQL, Spring Data JPA though special SELECT query, after that don't need to request hand by hand using EntityManager refresh
        nativeQuery = true) // <-- PHẢI THÊM nativeQuery = true
    Optional<Inventory> decreaseQuantityAndIncreaseReservedAtomic(
        @Param("productId") UUID productId,
        @Param("productSkuId") UUID productSkuId,
        @Param("quantity") Integer quantity
    );


    // @Modifying
    @Query(value = "UPDATE inventories " +
                "SET quantity_available = quantity_available + :quantity, " +
                "quantity_reserved = quantity_reserved - :quantity " +
                "WHERE product_id = :productId " +
                "AND product_sku_id = :productSkuId " +
                "AND quantity_reserved >= :quantity " +
                "RETURNING *",
        nativeQuery = true) // <-- PHẢI THÊM nativeQuery = true
    Optional<Inventory> increaseQuantityAndDecreaseReservedAtomic(
        @Param("productId") UUID productId,
        @Param("productSkuId") UUID productSkuId,
        @Param("quantity") Integer quantity
    );

    List<Inventory> findTop100ByOrderByCreatedAtDesc();

    @Query(value = "UPDATE inventories " +
                "SET quantity_available = quantity_available + :quantity, " +
                "quantity_reserved = quantity_reserved - :quantity " +
                "WHERE product_id = :productId " +
                "AND product_sku_id = :productSkuId " +
                "AND quantity_reserved >= :quantity " +
                "RETURNING *",
        nativeQuery = true) // <-- PHẢI THÊM nativeQuery = true
    Optional<Inventory> deductionQuantity(
        @Param("productId") UUID productId,
        @Param("productSkuId") UUID productSkuId,
        @Param("quantity") Integer quantity
    );
}
