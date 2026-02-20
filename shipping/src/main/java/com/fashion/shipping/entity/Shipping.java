package com.fashion.shipping.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.shipping.common.annotation.Searchable;
import com.fashion.shipping.common.enums.ShippingEnum;
import com.fashion.shipping.common.enums.ShippingProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "shippings",
    indexes = {
        @Index(name = "idx_shipping_order_created_at", columnList = "order_created_at"),
        @Index(name = "idx_shipping_code_unique", columnList = "tracking_code, created_at"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(ShippingId.class) // Primary key class when using composite keys (partition)
public class Shipping extends AbstractAuditingPartitionEntity<UUID>{
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

    @Column(name = "delivered_at")
    LocalDateTime deliveredAt;
   
    @Column(name = "estimated_date")
    LocalDateTime estimatedDate;
    
    @Searchable
    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    ShippingProvider provider;

    @Column(name = "shipping_at")
    LocalDateTime shippingAt;
    
    @Column(name = "shipping_fee", nullable = false)
    BigDecimal shippingFee;

    @Searchable
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    ShippingEnum status;

    @Column(name = "tracking_code")
    String trackingCode;

    @Column(name = "order_id", unique = true, nullable = false)
    UUID orderId;
    
    @Column(name = "event_id", nullable = false)
    UUID eventId;
    
    @Column(name = "order_code", unique = true, nullable = false)
    String orderCode;

    @Column(name = "order_created_at", nullable = false, updatable = false)
    Instant orderCreatedAt;

    @Column(name = "activated")
    Boolean activated;
}
