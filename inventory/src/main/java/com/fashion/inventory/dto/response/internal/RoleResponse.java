package com.fashion.inventory.dto.response.internal;

import java.time.Instant;
import java.util.List;

import com.fashion.inventory.dto.response.internal.PermissionResponse.InnerPermissionResponse;

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
public class RoleResponse {
    Long id;
    String name;
    String slug;
    boolean activated;
    String createdBy;
    String updatedBy;
    Instant createdAt;
    Instant updatedAt;
    List<InnerPermissionResponse> permissions;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerRoleResponse {
        Long id;
        String name;
        String slug;
    }
}
