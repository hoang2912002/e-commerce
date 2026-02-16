package com.fashion.order.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonAppend.Attr;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "order_details",
    indexes = {
        @Index(name = "idx_order_detail_order_id", columnList = "order_id"),
        @Index(name = "idx_order_detail_product_id", columnList = "product_id"),
        @Index(name = "idx_order_detail_product_sku_id", columnList = "product_sku_id"),
        @Index(name = "idx_order_detail_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(OrderDetailId.class) // Primary key class when using composite keys (partition)
public class OrderDetail extends AbstractAuditingPartitionEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_detail_seq")
    @SequenceGenerator(name = "order_detail_seq", sequenceName = "order_detail_seq", allocationSize = 1)
    @Column(name = "id")
    Long id;
    
    @Override
    public Long getId() {
        return this.id;
    }

    @Id
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Column(name = "order_created_at", nullable = false, updatable = false)
    Instant orderCreatedAt;

    @Column(name = "price_original", nullable = false)
    BigDecimal priceOriginal; //giá gốc
    
    @Column(name = "price", nullable = false)
    BigDecimal price; //giá sau giảm
    
    @Column(name = "promotion_discount", nullable = false)
    BigDecimal promotionDiscount; //giảm bao nhiêu = promotion * quantity 
    
    @Column(name = "quantity", nullable = false)
    Integer quantity;
    
    @Column(name = "total_price", nullable = false)
    BigDecimal totalPrice; //price * quantity
    
    @Column(name = "activated", nullable = false)
    Boolean activated;

    @Column(name = "product_id", nullable = false)
    UUID productId;
    
    @Column(name = "product_sku_id", nullable = false)
    UUID productSkuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "order_created_at", referencedColumnName = "created_at", insertable = false, updatable = false)
    })
    Order order;

    @Column(name = "order_id")
    UUID orderId;
}
