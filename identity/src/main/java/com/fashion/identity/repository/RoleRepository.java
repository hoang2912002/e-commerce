package com.fashion.identity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role>{
    Role findBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    Role findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Role r WHERE r.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Role lockRoleById(@Param("id") Long id);
}
