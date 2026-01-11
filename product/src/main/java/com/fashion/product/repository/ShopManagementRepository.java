package com.fashion.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.product.entity.Promotion;
import com.fashion.product.entity.ShopManagement;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ShopManagementRepository extends JpaRepository<ShopManagement, UUID>, JpaSpecificationExecutor<ShopManagement>{
    @Query("SELECT s FROM ShopManagement s WHERE s.slug = :slug")
    Optional<ShopManagement> findDuplicateForCreate(
        @Param("slug") String slug
    );
    
    @Query("SELECT s FROM ShopManagement s WHERE s.slug = :slug and s.id != :id")
    Optional<ShopManagement> findDuplicateForUpdate(
        @Param("slug") String slug,
        @Param("id") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShopManagement s WHERE s.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    ShopManagement lockShopManagementById(@Param("id") UUID id);
}
