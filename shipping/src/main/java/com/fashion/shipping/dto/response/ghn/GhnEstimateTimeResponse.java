package com.fashion.shipping.dto.response.ghn;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class GhnEstimateTimeResponse {

    @JsonProperty("leadtime")
    private Long leadtime;
    
    @JsonProperty("leadtime_order")
    private InnerLeadTimeResponse leadTimeOrder;

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class InnerLeadTimeResponse {
        @JsonProperty("from_estimate_date")
        private Instant fromEstimateDate;
        
        @JsonProperty("to_estimate_date")
        private Instant toEstimateDate;
        
    }

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class APIResponseGhnLeadTime {
        private Integer code;
        private String message;
        private GhnEstimateTimeResponse data; 
    }
}
