package com.fashion.product.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "shop_managements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopManagement extends AbstractAuditingEntity<UUID>{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Column(name = "name", length = 255, nullable = false)
    String name;
    
    @Column(name = "slug", length = 255, nullable = false)
    String slug;

    @Column(name = "business_name", length = 255)
    String businessName;
    
    @Column(name = "business_no", length = 255)
    String businessNo;

    @Column(name = "business_date_issue")
    LocalDate businessDateIssue;

    @Column(name = "business_place")
    String businessPlace;

    @Column(name = "tax_code", length = 255)
    String taxCode;

    @Column(name = "business_type")
    Integer businessType;

    @Column(name = "account_name")
    String accountName;

    @Column(name = "account_number")
    String accountNumber;

    @Column(name = "bank_name")
    String bankName;

    @Column(name = "bank_branch")
    String bankBranch;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "address_id")
    UUID addressId;

    @Column(name = "user_id")
    UUID userId;

    @Column(name = "activated")
    Boolean activated;

    @OneToMany( mappedBy = "shopManagement", fetch = FetchType.LAZY)
    List<Product> products = new ArrayList<>();
}
