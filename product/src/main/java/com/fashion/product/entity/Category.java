package com.fashion.product.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fashion.product.common.annotation.Searchable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "categories",
    indexes = {
        @Index(name = "idx_category_parent_id", columnList = "parent_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category extends AbstractAuditingEntity<UUID>{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Searchable
    @Column(name = "name", length = 100, nullable = false)
    String name;

    @Searchable
    @Column(name = "slug", length = 100, nullable = false, unique = true)
    String slug;

    @Column(name = "activated")
    Boolean activated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY) 
    Set<Category> children = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    List<Product> products;
    
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    List<PromotionProduct> promotionProducts;
}
