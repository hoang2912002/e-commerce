package com.fashion.payment.entity;

import java.time.Instant;
import java.util.UUID;

import com.fashion.payment.common.enums.PaymentEnum;
import com.fasterxml.jackson.databind.annotation.JsonAppend.Attr;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(PaymentTransactionId.class) // Primary key class when using composite keys (partition)
public class PaymentTransaction extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_transaction_seq")
    @SequenceGenerator(name = "payment_transaction_seq", sequenceName = "payment_transaction_seq", allocationSize = 1)
    @Column(name = "id")
    Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "transaction_id", nullable = false)
    String transactionId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    PaymentEnum status; // Private status for transaction per

    @Column(name = "raw_response",columnDefinition = "TEXT")
    String rawResponse; // JSON response

    @Column(name = "note",columnDefinition = "TEXT")
    String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "payment_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "created_at", referencedColumnName = "created_at", insertable = false, updatable = false)
    })
    Payment payment;

    @Column(name = "payment_id", nullable = false)
    UUID paymentId;
}
