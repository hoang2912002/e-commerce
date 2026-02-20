package com.fashion.shipping.dto.response.ghn;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnAddressIdResponse {
    Long provinceId;
    Long districtId;
    String wardCode;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WarmResultGhn {
        @Builder.Default
        Integer districtCount = 0;
        @Builder.Default
        Integer wardCount = 0;
        @Builder.Default
        Integer failedCount = 0;
    }
}
