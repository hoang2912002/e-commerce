package com.fashion.product.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.dto.request.RoleRequest.InnerRoleRequest;
import com.fashion.product.dto.request.UserRequest.InnerUserRequest;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ApprovalMasterRequest {
    public interface Create{};
    public interface Update{};
    @NotNull(message = "approval.master.id.notNull", groups = {Update.class})
    UUID id;

    @NotBlank(message = "approval.master.entityType.notNull", groups = {Create.class, Update.class})
    String entityType; // -- PRODUCT, INVENTORY, PURCHASE_ORDER...
    
    @NotNull(message = "approval.master.step.notNull", groups = {Create.class, Update.class})
    Integer step; // 1, 2, 3, 4

    @NotNull(message = "approval.master.status.notNull", groups = {Create.class, Update.class})
    @Enumerated(EnumType.STRING)
    ApprovalMasterEnum status;

    Boolean required;

    @Valid
    InnerRoleRequest role;

    @Valid
    InnerUserRequest user;
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
    public static class InnerApprovalMasterRequest {
        UUID id;
    }
}
