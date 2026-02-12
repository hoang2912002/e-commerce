package com.fashion.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.entity.ApprovalHistory;
import com.fashion.product.entity.ShopManagement;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long>, JpaSpecificationExecutor<ApprovalHistory>{
    List<ApprovalHistory> findAllByRequestId(UUID requestId);

    @EntityGraph(attributePaths = "approvalMaster")
    @Transactional
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

    boolean existsByEventIdAndRequestId(UUID eventId, UUID requestId);

    
    Optional<ApprovalHistory> findFirstByRequestIdAndApprovalMasterIdInOrderByApprovedAtDesc(UUID requestId, List<UUID> approvalMasterIds);
    Optional<ApprovalHistory> findTopByApprovalMasterIdOrderByApprovedAtDesc(UUID approvalMasterId);

    @Transactional
    @EntityGraph(attributePaths = "approvalMaster")
    Optional<ApprovalHistory> findFirstByRequestIdOrderByApprovedAtDesc(UUID requestId);
}
