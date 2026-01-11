package com.fashion.product.dto.response;

import java.time.Instant;
import java.util.List;

import com.fashion.product.dto.response.OptionValueResponse.InnerOptionValueResponse;

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
public class OptionResponse {
    Long id;
    String name;
    String slug;
    String createdBy;
    Instant createdAt;
    Boolean activated;
    Instant updatedAt;
    String updatedBy;
    List<InnerOptionValueResponse> optionValues;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerOptionResponse {
        Long id;
        String name;
        String slug;
    }
}
