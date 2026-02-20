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
public class GhnProvinceResponse {
    @JsonProperty("ProvinceID")
    private Long provinceId;

    @JsonProperty("ProvinceName")
    private String provinceName;

    @JsonProperty("CountryID")
    private Long countryId;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("NameExtension")
    private List<String> nameExtension;
    

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class InnerGhnProvinceResponse {
        private Long provinceId;
    }

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class APIResponseGhnProvince { 
    
        private Integer code;
        private String message;
        
        private List<GhnProvinceResponse> data; 
    }
}
