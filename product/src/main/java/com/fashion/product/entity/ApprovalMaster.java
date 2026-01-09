package com.fashion.product.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
@Table(name = "approval_masters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovalMaster extends AbstractAuditingEntity<UUID>{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Column(name = "entity_type", length = 100, nullable = false)
    String entityType; // -- PRODUCT, INVENTORY, PURCHASE_ORDER...

    @Column(name = "step", nullable = false)
    Integer step; // 1, 2, 3, 4
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    ApprovalMasterEnum status;

    @Column(name = "role_id", nullable = false)
    Long roleId;

    @Column(name = "user_id")
    UUID userId;

    @Column(name = "required")
    Boolean required = Boolean.FALSE;

    @Column(name = "activated")
    Boolean activated;

    @OneToMany( 
        mappedBy = "approvalMaster", 
        fetch = FetchType.LAZY
    )
    List<ApprovalHistory> approvalHistories = new ArrayList<>();
}
