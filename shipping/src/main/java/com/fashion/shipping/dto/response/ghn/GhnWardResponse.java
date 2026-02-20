package com.fashion.shipping.dto.response.ghn;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class GhnWardResponse {
    @JsonProperty("WardCode")
    private String wardCode;

    @JsonProperty("DistrictID")
    private Long districtId;

    @JsonProperty("WardName")
    private String wardName;

    @JsonProperty("NameExtension")
    private List<String> nameExtension;


    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class InnerGhnWardResponse {
        private String wardCode;
    }

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class APIResponseGhnWard { 
    
        private Integer code;
        private String message;
        
        private List<GhnWardResponse> data; 
    }
}
