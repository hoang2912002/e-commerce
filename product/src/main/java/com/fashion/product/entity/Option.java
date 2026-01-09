package com.fashion.product.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Option extends AbstractAuditingEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Column(name = "name", length = 100)
    String name;

    @Column(name = "slug", length = 100)
    String slug;

    @Column(name = "activated")
    Boolean activated;

    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
    List<OptionValue> optionValues;

    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Variant> variants;
}
