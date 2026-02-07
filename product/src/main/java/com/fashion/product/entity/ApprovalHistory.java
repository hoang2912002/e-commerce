package com.fashion.product.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.product.common.annotation.Searchable;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
@Table(name = "approval_histories",
    indexes = {
        @Index(name = "idx_history_request_id", columnList = "request_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovalHistory extends AbstractAuditingEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Column(name = "request_id", nullable = false)
    UUID requestId;

    @Searchable
    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(name = "activated")
    Boolean activated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_master_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ApprovalMaster approvalMaster;
    
}
