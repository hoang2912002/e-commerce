package com.fashion.order.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetail extends AbstractAuditingEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    Long id;
    
    @Override
    public Long getId() {
        return this.id;
    }
    
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
    @JoinColumn(name = "order_id")
    Order order;
}
