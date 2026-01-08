package com.fashion.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.OptimisticLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.identity.entity.Permission;
import com.fashion.identity.entity.User;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission>{
    List<Permission> findAllByService(String service);
    List<Permission> findAllByIdIn(List<Long> id);

    @Query("SELECT p FROM Permission p WHERE p.apiPath = :ap AND p.method = :md AND p.service = :sv")
    Optional<Permission> findDuplicateForCreate(
        @Param("ap") String apiPath, 
        @Param("md") String method, 
        @Param("sv") String service
    );

    @Query("SELECT p FROM Permission p WHERE (p.apiPath = :ap AND p.method = :md AND p.service = :sv) AND p.id <> :id")
    Optional<Permission> findDuplicateForUpdate(
        @Param("ap") String apiPath, 
        @Param("md") String method, 
        @Param("sv") String service,
        @Param("id") Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Permission p WHERE p.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Permission lockPermissionById(@Param("id") Long id);

}
