package com.fashion.order.entity;

import java.time.Instant;
import java.util.UUID;

import com.fashion.order.common.annotation.Searchable;
import com.fashion.order.common.enums.PaymentEnum;
import com.fashion.order.common.enums.SagaStateStatusEnum;
import com.fashion.order.common.enums.SagaStateStepEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "saga_states",
    indexes = {
        @Index(name = "idx_saga_states_order_id", columnList = "order_id"),
        @Index(name = "idx_saga_states_order_code", columnList = "order_code"),
        @Index(name = "idx_saga_states_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(SagaStateId.class) 
public class SagaState extends AbstractAuditingPartitionEntity<UUID>{
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Id
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Column(name = "order_id", unique = true, nullable = false)
    UUID orderId;
    
    @Column(name = "order_code", unique = true, nullable = false)
    String orderCode;

    @Column(name = "order_created_at", nullable = false, updatable = false)
    Instant orderCreatedAt;

    @Searchable
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    SagaStateStatusEnum status; 


    @Searchable
    @Column(name = "step", nullable = false)
    @Enumerated(EnumType.STRING)
    SagaStateStepEnum step; 

    @Column(name = "payload", columnDefinition = "TEXT")
    String payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "order_created_at", referencedColumnName = "created_at", insertable = false, updatable = false)
    })
    Order order;
}
