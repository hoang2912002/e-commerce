package com.fashion.product.dto.response;

import java.time.Instant;
import java.util.UUID;

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
public class CategoryResponse extends VersionResponse {
    UUID id;
    String name;
    String slug;
    InnerCategoryResponse parent;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
    Boolean activated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerCategoryResponse {
        UUID id;
        String name;
        String slug;
    }
}
