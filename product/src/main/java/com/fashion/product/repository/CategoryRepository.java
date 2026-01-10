package com.fashion.product.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.fashion.product.entity.Category;

import feign.Param;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    Optional<Category> findBySlug(String slug);
    Optional<Category> findBySlugAndIdNot(String slug, UUID id);
    List<Category> findAllByIdIn(List<UUID> id);
    List<Category> findAllByIdNotIn(Collection<UUID> id);

    // @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    @EntityGraph(attributePaths = "children")
    @Query("SELECT DISTINCT c FROM Category c WHERE c.id IN :ids")
    List<Category> findAllByIdWithChildren(@Param("ids") List<UUID> ids);
}
