package com.fashion.shipping.dto.response.ghn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class GhnPackageServiceResponse {
    private Long serviceId;
    private String shortName;
    private Integer serviceTypeId;
}