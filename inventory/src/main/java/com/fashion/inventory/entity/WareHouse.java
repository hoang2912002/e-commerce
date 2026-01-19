package com.fashion.inventory.entity;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fashion.inventory.common.annotation.Searchable;
import com.fashion.inventory.common.enums.WareHouseStatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "ware_houses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WareHouse extends AbstractAuditingEntity<UUID>{
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

    @Searchable
    @Column(name = "code", unique = true, nullable = false, length = 10)
    String code;

    @Searchable
    @Column(name = "name", nullable = false)
    String name;

    @Searchable
    @Column(name = "location", nullable =  false)
    String location;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    WareHouseStatusEnum status;

    @OneToMany(mappedBy = "wareHouse", fetch = FetchType.LAZY, orphanRemoval = true)
    List<Inventory> inventories;
    
    @OneToMany(mappedBy = "wareHouse", fetch = FetchType.LAZY, orphanRemoval = true)
    List<Inventory> inventoryTransactions;
}
