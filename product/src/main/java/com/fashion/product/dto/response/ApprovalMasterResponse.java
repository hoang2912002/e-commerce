package com.fashion.product.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.dto.request.RoleRequest.InnerRoleRequest;
import com.fashion.product.dto.request.UserRequest.InnerUserRequest;
import com.fashion.product.dto.response.RoleResponse.InnerRoleResponse;
import com.fashion.product.dto.response.UserResponse.InnerUserResponse;

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
