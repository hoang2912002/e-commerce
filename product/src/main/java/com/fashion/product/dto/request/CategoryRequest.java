package com.fashion.product.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CategoryRequest {
    public interface Create {}
    public interface Update {}

    @NotBlank(message = "category.id.notNull", groups = Update.class)
    UUID id;

    @NotBlank(message = "category.name.notNull", groups = { Update.class, Create.class })
    String name;
    String slug;
    InnerCategoryRequest parent;
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
    public static class InnerCategoryRequest {
        @NotNull(message = "category.id.notNull", groups = { 
            Update.class, 
            Create.class,
            ProductRequest.Create.class,
            ProductRequest.Update.class,
        })
        UUID id;
    }
}
