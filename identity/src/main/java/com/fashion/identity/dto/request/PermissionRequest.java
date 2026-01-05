package com.fashion.identity.dto.request;

import java.time.Instant;

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
public class PermissionRequest {
    Long id;
    String name;
    String apiPath;
    String method;
    String module;
    String service;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerPermissionRequest {
        Long id;
        String name;
        String apiPath;
        String method;
        String module;
        String service;
    }
}
