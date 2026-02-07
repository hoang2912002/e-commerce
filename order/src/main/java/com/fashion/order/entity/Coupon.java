package com.fashion.order.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fashion.order.common.annotation.Searchable;
import com.fashion.order.common.enums.CouponEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "coupons",
    indexes = {
        @Index(name = "idx_coupon_code", columnList = "code"),
        @Index(name = "idx_coupon_name", columnList = "name"),
        @Index(name = "idx_coupon_dates", columnList = "start_date, end_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Coupon extends AbstractAuditingEntity<UUID>{
   @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    UUID id;

    @Override
    public UUID getId() {
        return this.id;
    } 

    @Version
    Long version;

    @Searchable
    @Column(name = "name", nullable = false)
    String name;

    @Searchable
    @Column(name = "code", nullable = false, unique = true)
    String code;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    CouponEnum type;

    @Column(name = "stock")
    Integer stock;

    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    LocalDateTime endDate;

    @Column(name = "coupon_amount", nullable = false)
    BigDecimal couponAmount; // giảm bao nhiêu

    @Column(name = "activated")
    Boolean activated;

    @OneToMany( mappedBy = "coupon", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    List<Order> orders = new ArrayList<>();
}
