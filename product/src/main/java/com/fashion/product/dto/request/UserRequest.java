package com.fashion.product.dto.request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fashion.product.common.enums.GenderEnum;
import com.fashion.product.dto.request.AddressRequest.InnerAddressRequest;
import com.fashion.product.dto.request.RoleRequest.InnerRoleRequest;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    UUID id;
    String fullName;
    String email;
    String password;
    @Enumerated(EnumType.STRING)
    GenderEnum gender;
    Instant dob;
    Integer age;
    InnerRoleRequest role;
    List<InnerAddressRequest> addresses;
    boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerUserRequest {
        UUID id;
    } 
}
