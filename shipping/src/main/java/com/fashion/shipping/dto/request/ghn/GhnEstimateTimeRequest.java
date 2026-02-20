package com.fashion.shipping.dto.request.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class GhnEstimateTimeRequest {
    @JsonProperty("service_type_id")
    private Integer service_type_id;

    @JsonProperty("from_district_id")
    private Long from_district_id;
    
    @JsonProperty("from_ward_code")
    private String from_ward_code;

    @JsonProperty("to_district_id")
    private Long to_district_id;

    @JsonProperty("to_ward_code")
    private String to_ward_code;
}
