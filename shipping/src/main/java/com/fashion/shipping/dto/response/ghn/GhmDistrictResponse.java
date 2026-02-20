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
public class GhmDistrictResponse {
    @JsonProperty("DistrictID")
    private Long districtId;

    @JsonProperty("ProvinceID")
    private Long provinceId;

    @JsonProperty("DistrictName")
    private String districtName;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("NameExtension")
    private List<String> nameExtension;

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class InnerGhmDistrictResponse {
        private Long districtId;
    }

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class APIResponseGhnDistrict { 
    
        private Integer code;
        private String message;
        
        private List<GhmDistrictResponse> data; 
    }
}
