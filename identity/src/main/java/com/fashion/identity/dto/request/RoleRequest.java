package com.fashion.identity.dto.request;

import java.time.Instant;
import java.util.List;

import com.fashion.identity.dto.request.PermissionRequest.InnerPermissionRequest;

import jakarta.validation.constraints.NotNull;
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
public class RoleRequest {
    Long id;
    String name;
    String slug;
    String createdBy;
    String updatedBy;
    Instant createdAt;
    Instant updatedAt;
    List<InnerPermissionRequest> permissions;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerRoleRequest {
        @NotNull(message = "role.id.notNull")
        Long id;
    }
}
