package com.fashion.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.product.entity.Option;

public interface OptionRepository extends JpaRepository<Option, Long>, JpaSpecificationExecutor<Option>{
    Optional<Option> findBySlug(String slug);
    Optional<Option> findBySlugAndIdNot(String slug, Long id);
}
