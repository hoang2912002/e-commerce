package com.fashion.inventory.dto.response.internal;

import java.time.Instant;
import java.util.UUID;

import com.fashion.inventory.common.enums.ApprovalMasterEnum;
import com.fashion.inventory.dto.response.internal.RoleResponse.InnerRoleResponse;
import com.fashion.inventory.dto.response.internal.UserResponse.InnerUserResponse;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovalMasterResponse {
    UUID id;
    String entityType;
    Integer step;
    @Enumerated(EnumType.STRING)
    ApprovalMasterEnum status;
    Boolean required;
    InnerRoleResponse role;
    InnerUserResponse user;
    Boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerApprovalMasterResponse {
        UUID id;
    }
}
