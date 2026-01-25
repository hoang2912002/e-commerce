package com.fashion.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fashion.order.common.enums.OrderEnum;
import com.fashion.order.common.enums.PaymentEnum;
import com.fashion.order.common.enums.ShippingEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order extends AbstractAuditingEntity<UUID>{
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

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    PaymentEnum paymentStatus; // PENDING, PAID, REFUNDED

    @Column(name = "payment_method")
    String paymentMethod;

    @Column(name = "shipping_status")
    @Enumerated(EnumType.STRING)
    ShippingEnum shippingStatus; // AT_WAREHOUSE, DELIVERING, DELIVERED

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    OrderEnum status;

    @Column(name = "total_item", nullable = false)
    Integer totalItem;

    @Column(name = "total_price", nullable = false)
    BigDecimal totalPrice;       // giá tạm tính

    @Column(name = "discount_price")
    BigDecimal discountPrice;    // tổng giảm giá = coupon + (n * promotion)

    @Column(name = "final_price")
    BigDecimal finalPrice;       // giá phải trả

    @Column(name = "shipping_fee")
    BigDecimal shippingFee;

    @Column(name = "user_id")
    UUID userId;

    @Column(name = "shipping_id")
    UUID shippingId;

    @Column(name = "address_id")
    UUID addressId;

    @Column(name = "payment_id")
    UUID paymentId;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    @Column(name = "activated")
    Boolean activated;
    
    @Column(name = "code")
    String code;

    @Column(name = "receiver_name")
    String receiverName;
    @Column(name = "receiver_email")
    String receiverEmail;
    @Column(name = "receiver_phone")
    String receiverPhone;
    @Column(name = "receiver_address")
    String receiverAddress;
    @Column(name = "receiver_province")
    String receiverProvince;
    @Column(name = "receiver_district")
    String receiverDistrict;
    @Column(name = "receiver_ward")
    String receiverWard;

    @OneToMany( mappedBy = "order", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    List<OrderDetail> orderDetails = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    Coupon coupon;
}
