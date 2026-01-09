package com.fashion.product.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "option_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionValue extends AbstractAuditingEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    Option option;

    @OneToMany(mappedBy = "optionValue", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Variant> variants;
}
