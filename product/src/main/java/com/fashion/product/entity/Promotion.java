package com.fashion.product.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fashion.product.common.annotation.Searchable;
import com.fashion.product.common.enums.PromotionEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "promotions",
    indexes = {
        @Index(name = "idx_promotion_code", columnList = "code"),
        @Index(name = "idx_promotion_name", columnList = "name"),
        @Index(name = "idx_promotion_start_end_date", columnList = "start_date, end_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Promotion extends AbstractAuditingEntity<UUID>{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Searchable
    @Column(name = "code", unique = true, length = 50)
    String code;
    
    @Searchable
    @Column(name = "name", length = 100)
    String name;

    @Column(name = "description", columnDefinition= "TEXT")
    String description;

    @Column(name = "discount_percent")
    Integer discountPercent;

    @Column(name = "min_discount_amount")
    BigDecimal minDiscountAmount;
    
    @Column(name = "max_discount_amount")
    BigDecimal maxDiscountAmount;

    @Column(name = "quantity")
    Integer quantity;

    @Enumerated(EnumType.STRING)
    PromotionEnum discountType;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "activated")
    Boolean activated;

    @Column(name = "option_promotion")
    byte optionPromotion;

    @Column(name = "event_id", nullable = false) // Check Idempotency
    UUID eventId;

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    List<PromotionProduct> promotionProducts = new ArrayList<>();
}
