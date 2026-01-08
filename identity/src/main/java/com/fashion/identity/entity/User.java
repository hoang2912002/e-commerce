package com.fashion.identity.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fashion.identity.common.annotation.Searchable;
import com.fashion.identity.common.enums.GenderEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends AbstractAuditingEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR) // Ép kiểu JDBC về CHAR
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Searchable
    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Searchable
    @Column(name = "email", unique = true, length = 100)
    String email;

    @Column(name = "password", nullable = false)
    String password;

    @Searchable
    @Column(name = "phone_number", nullable = false, length = 11)
    String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    GenderEnum gender;

    @Column(name = "dob")
    LocalDate dob;

    @Searchable
    @Column(name = "user_name", unique = true, length = 100)
    String userName;

    @Column(name = "refresh_token", columnDefinition = "MEDIUMTEXT")
    String refreshToken;

    @Column(name = "email_verified", nullable = false)
    boolean emailVerified = false;

    @Column(name = "verification_code", length = 64)
    String verificationCode;

    @Column(name = "verification_expiration")
    LocalDateTime verificationExpiration;

    @Column(name = "activated")
    Boolean activated;

    @OneToMany( mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    List<Address> addresses = new ArrayList<>();

    @ManyToOne()
    @JoinColumn(name = "role_id")
    private Role role;
}
