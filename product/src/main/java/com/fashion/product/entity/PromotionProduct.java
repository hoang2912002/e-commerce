package com.fashion.product.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "promotion_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionProduct extends AbstractAuditingEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Column(name = "activated")
    Boolean activated;

    @ManyToOne()
    @JoinColumn(name = "product_id")
    Product product;
    
    @ManyToOne()
    @JoinColumn(name = "promotion_id")
    Promotion promotion;
    
    @ManyToOne()
    @JoinColumn(name = "category_id")
    Category category;
}
