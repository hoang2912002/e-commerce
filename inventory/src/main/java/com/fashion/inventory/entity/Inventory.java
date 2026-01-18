package com.fashion.inventory.entity;

import java.util.Locale.Category;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "inventories",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_sku_id", "warehouse_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Inventory extends AbstractAuditingEntity<UUID>{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Column(name = "activated")
    Boolean activated;

    @Column(name = "product_id", nullable = false)
    UUID productId;

    @Column(name = "product_sku_id", nullable = false)
    UUID productSkuId;

    @Column(name = "quantity_available")
    Integer quantityAvailable;
    
    @Column(name = "quantity_reserved")
    Integer quantityReserved;
    
    @Column(name = "quantity_sold")
    Integer quantitySold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    WareHouse wareHouse;
}
