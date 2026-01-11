package com.fashion.identity.entity;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address extends AbstractAuditingEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR) // Ép kiểu JDBC về CHAR
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Column(name = "address", length = 255)
    String address;

    @Column(name = "province", length = 100)
    String province;
    
    @Column(name = "district", length = 100)
    String district;

    @Column(name = "ward", length = 100)
    String ward;

    @Column(name = "activated")
    Boolean activated;
    
    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = true)
    @JsonBackReference
    private User user;

    @Column(name = "shop_management_id")
    UUID shopManagementId;

    @Column(name = "current_user_address")
    Boolean currentUserAddress;
}
