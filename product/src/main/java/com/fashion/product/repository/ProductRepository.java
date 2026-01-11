package com.fashion.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fashion.product.entity.Product;
import com.fashion.product.entity.Promotion;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product>{
    @Query("SELECT p FROM Product p WHERE p.slug = :s")
    Optional<Product> findDuplicateForCreate(
        @Param("s") String slug
    );
    
    @Query("SELECT p FROM Product p WHERE p.slug = :s and p.id != :i")
    Optional<Product> findDuplicateForUpdate(
        @Param("s") String slug,
        @Param("i") UUID id
    );

    List<Product> findAllByIdIn(List<UUID> id);
}
