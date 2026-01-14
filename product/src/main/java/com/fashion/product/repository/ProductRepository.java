package com.fashion.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.product.entity.Product;
import com.fashion.product.entity.Promotion;
import com.fashion.product.entity.ShopManagement;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product>{
    @Query("SELECT p FROM Product p WHERE p.slug = :slug AND p.category.id =:cateId AND p.shopManagement.id =:sm")
    Optional<Product> findDuplicateForCreate(
        @Param("slug") String slug,
        @Param("cateId") UUID cateId,
        @Param("sm") UUID sm
    );
    
    @Query("SELECT p FROM Product p WHERE p.slug = :slug AND p.category.id =:cateId AND p.shopManagement.id =:sm and p.id != :i")
    Optional<Product> findDuplicateForUpdate(
        @Param("slug") String slug,
        @Param("cateId") UUID categoryId,
        @Param("sm") UUID shopManagementId,
        @Param("i") UUID id
    );

    List<Product> findAllByIdIn(List<UUID> id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<Product> lockProductById(@Param("id") UUID id);
}
