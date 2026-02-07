package com.fashion.inventory.entity;

import java.util.UUID;

import com.fashion.inventory.common.enums.InventoryTransactionReferenceTypeEnum;
import com.fashion.inventory.common.enums.InventoryTransactionTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "inventory_transactions",
    indexes = {
        @Index(name = "idx_inv_trx_product_sku", columnList = "product_sku_id"),
        @Index(name = "idx_inv_trx_reference", columnList = "reference_id"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransaction extends AbstractAuditingEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Column(name = "activated")
    Boolean activated;

    @Column(name = "product_sku_id", nullable = false)
    UUID productSkuId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    InventoryTransactionTypeEnum type;  // Hành động tồn kho

    @Column(name = "quantity_change", nullable = false)
    Integer quantityChange;

    @Column(name = "before_quantity", nullable = false)
    Integer beforeQuantity;
    
    @Column(name = "after_quantity", nullable = false)
    Integer afterQuantity;

    @Column(name = "reference_type", nullable = false)
    @Enumerated(EnumType.STRING)
    InventoryTransactionReferenceTypeEnum referenceType;

    @Column(name = "reference_id", nullable = false)
    UUID referenceId;

    @Column(name = "note")
    String note;

    @Column(name = "event_id", nullable = false) // Check Idempotency khi tạo tồn kho từ product-service
    UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    WareHouse wareHouse;
}
