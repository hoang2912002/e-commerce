package com.fashion.product.dto.request;

import java.time.Instant;
import java.util.List;


import jakarta.validation.constraints.NotBlank;
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
    public interface Create {}
    public interface Update {}
    @NotNull(message = "role.id.notNull", groups = Update.class)
    Long id;

    @NotBlank(message = "role.name.notNull", groups = {Create.class, Update.class})
    String name;
    String slug;
    String createdBy;
    String updatedBy;
    Instant createdAt;
    Instant updatedAt;
    // List<InnerPermissionRequest> permissions;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerRoleRequest {
        @NotNull(message = "role.id.notNull", groups = {ApprovalMasterRequest.Create.class, ApprovalMasterRequest.Update.class})
        Long id;
    }
}
