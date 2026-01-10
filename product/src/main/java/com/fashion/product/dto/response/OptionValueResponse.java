package com.fashion.product.dto.response;

import java.time.Instant;

import com.fashion.product.dto.response.OptionResponse.InnerOptionResponse;

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
public class OptionValueResponse {
    Long id;
    String value;
    String slug;
    String createdBy;
    Instant createdAt;
    boolean activated;
    Instant updatedAt;
    boolean updatedBy;
    InnerOptionResponse option;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerOptionValueResponse {
        Long id;
        String value;
        String slug;
    }
}
