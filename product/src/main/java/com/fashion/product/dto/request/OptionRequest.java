package com.fashion.product.dto.request;

import java.time.Instant;

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
public class OptionRequest {
    public interface Create {}
    public interface Update {}

    @NotNull(message = "option.id.notNull", groups = Update.class)
    Long id;

    @NotBlank(message = "option.name.notNull", groups = {Create.class,Update.class})
    String name;
    String slug;
    String createdBy;
    Instant createdAt;
    Boolean activated;
    Instant updatedAt;
    String updatedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerOptionRequest {
        @NotNull(message = "option.id.notNull", groups = {OptionValueRequest.Create.class,OptionValueRequest.Update.class})
        Long id;
    }
}
