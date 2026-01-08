package com.fashion.identity.entity;

import java.util.List;

import com.fashion.identity.common.annotation.Searchable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Searchable
    @Column(name = "name", length = 100)
    String name;

    @Column(name = "api_path", length = 100)
    String apiPath;

    @Searchable
    @Column(name = "method", length = 10)
    String method;

    @Searchable
    @Column(name = "module", length = 100)
    String module;
    
    @Searchable
    @Column(name = "service", length = 100)
    String service;

    @Column(name = "activated")
    Boolean activated;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissions")
    @JsonIgnore
    private List<Role> roles;
}
