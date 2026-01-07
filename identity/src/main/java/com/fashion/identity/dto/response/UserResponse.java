package com.fashion.identity.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fashion.identity.common.enums.GenderEnum;
import com.fashion.identity.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.identity.dto.response.RoleResponse.InnerRoleResponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    LocalDate dob;
    String email;
    String fullName;
    GenderEnum gender;
    String phoneNumber;
    String userName;
    String avatar;
    boolean emailVerified;
    String verificationCode;
    LocalDateTime verificationExpiration; 
    boolean activated;
    RoleResponse role;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    List<InnerAddressResponse> addresses;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerUserResponse {
        UUID id;
        String fullName;
        String email;
        String phoneNumber;
        String avatar;
        InnerRoleResponse role;
    }
}
