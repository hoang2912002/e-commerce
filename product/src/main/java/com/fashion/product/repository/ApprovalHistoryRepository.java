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

import com.fashion.product.entity.ApprovalHistory;
import com.fashion.product.entity.ShopManagement;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long>, JpaSpecificationExecutor<ApprovalHistory>{
    List<ApprovalHistory> findAllByRequestId(UUID requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApprovalHistory a WHERE a.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<ApprovalHistory> lockApprovalHistoryById(@Param("id") Long id);

    @Query("SELECT a FROM ApprovalHistory a " +
       "WHERE a.requestId = :requestId " +
       "AND a.id IN :approvalHisIds " +
       "ORDER BY a.approvedAt ASC")
    List<ApprovalHistory> findAllForBusiness(
        @Param("approvalHisIds") List<UUID> approvalHisIds,
        @Param("requestId") UUID requestId
    );

    
    Optional<ApprovalHistory> findFirstByRequestIdAndApprovalMasterIdInOrderByApprovedAtDesc(UUID requestId, List<UUID> approvalMasterIds);
    Optional<ApprovalHistory> findFirstByRequestIdOrderByApprovedAtDesc(UUID requestId);
}
