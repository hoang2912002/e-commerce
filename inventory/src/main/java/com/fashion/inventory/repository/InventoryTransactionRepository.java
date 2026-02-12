package com.fashion.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.inventory.common.enums.InventoryTransactionReferenceTypeEnum;
import com.fashion.inventory.common.enums.InventoryTransactionTypeEnum;
import com.fashion.inventory.entity.Inventory;
import com.fashion.inventory.entity.InventoryTransaction;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction>{
    Boolean existsByEventIdAndReferenceTypeAndReferenceId(UUID eventId, InventoryTransactionReferenceTypeEnum referenceType, UUID referenceId);
    Boolean existsByEventId(UUID eventId);

    @Transactional
    Optional<InventoryTransaction> findFirstByProductSkuIdAndReferenceIdOrderByCreatedAtDesc(UUID productSkuId, UUID productId);

}
