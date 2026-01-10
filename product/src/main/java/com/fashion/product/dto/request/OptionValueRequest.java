package com.fashion.product.dto.request;

import java.time.Instant;

import com.fashion.product.dto.request.OptionRequest.InnerOptionRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionValueRequest {
    public interface Create {}
    public interface Update {}

    @NotNull(message = "option.value.id.notNull", groups = Update.class)
    Long id;

    @NotBlank(message = "option.value.value.notNull", groups = {Create.class,Update.class})
    String value;
    String slug;
    String createdBy;
    Instant createdAt;
    Boolean activated;
    Instant updatedAt;
    String updatedBy;

    @Valid
    InnerOptionRequest option;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerOptionValueRequest{
        Long id;
    }
}
