package com.fashion.shipping.entity;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
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
public abstract class AbstractAuditingPartitionEntity<T> implements Serializable{
    private static final long serialVersionUID = 1L; // Đây là version number của Serializable class

    public abstract T getId();

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    String createdBy;

    // @CreatedDate

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    Instant updatedAt = Instant.now();
}
