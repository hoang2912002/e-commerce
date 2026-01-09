package com.fashion.product.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "product_skus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSku extends AbstractAuditingEntity<UUID>{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Column(name = "sku", unique = true, length = 255)
    String sku;

    @Column(name = "price", nullable = true)
    BigDecimal price;

    @Column(name = "temp_stock", nullable = true)
    Integer tempStock;

    @Column(name = "activated")
    Boolean activated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @OneToMany(mappedBy = "productSku", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Variant> variants;
}
