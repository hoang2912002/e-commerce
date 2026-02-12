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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.entity.ApprovalMaster;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface ApprovalMasterRepository extends JpaRepository<ApprovalMaster, UUID>, JpaSpecificationExecutor<ApprovalMaster> {
    @Query("SELECT a FROM ApprovalMaster a WHERE " +
       "a.entityType = :entityType AND " +
       "a.status = :status AND " +
       "a.step = :step")
    Optional<ApprovalMaster> findDuplicateForCreate(
        @Param("entityType") String entityType,
        @Param("status") ApprovalMasterEnum status,
        @Param("step") Integer step
    );
    
    @Query("SELECT a FROM ApprovalMaster a WHERE " +
       "a.entityType = :entityType AND " +
       "a.status = :status AND " +
       "a.step = :step AND " +
       "a.id <> :id")
    Optional<ApprovalMaster> findDuplicateForUpdate(
        @Param("entityType") String entityType,
        @Param("status") ApprovalMasterEnum status,
        @Param("step") Integer step,
        @Param("id") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApprovalMaster a WHERE a.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    ApprovalMaster lockApprovalMasterById(@Param("id") UUID id);

    List<ApprovalMaster> findAllByEntityType(String entityType);

    @Transactional
    Optional<ApprovalMaster> findByEntityTypeAndStatus(String entityType, ApprovalMasterEnum aEnum);
}
