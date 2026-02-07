package com.fashion.order.entity;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractAuditingEntity<T> implements Serializable{
    private static final long serialVersionUID = 1L; // Đây là version number của Serializable class

    public abstract T getId();

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    Instant updatedAt = Instant.now();
}
