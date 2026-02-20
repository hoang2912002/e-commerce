package com.fashion.shipping.integration;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fashion.shipping.dto.request.ghn.GhnEstimateTimeRequest;
import com.fashion.shipping.dto.request.ghn.GhnShippingFeeRequest;
import com.fashion.shipping.dto.response.ghn.GhmDistrictResponse.APIResponseGhnDistrict;
import com.fashion.shipping.dto.response.ghn.GhnEstimateTimeResponse.APIResponseGhnLeadTime;
import com.fashion.shipping.dto.response.ghn.GhnProvinceResponse.APIResponseGhnProvince;
import com.fashion.shipping.dto.response.ghn.GhnShippingFeeResponse.APIResponseGhnShippingFee;
import com.fashion.shipping.dto.response.ghn.GhnWardResponse.APIResponseGhnWard;

@FeignClient(url = "${shipping.ghn.base-url-production}", name = "${service.shipping.name}")
public interface ShippingGhnClient {
    
    @PostMapping("/master-data/province")
    APIResponseGhnProvince getThirdPartyProvince(
        @RequestHeader("token") String token
    );

    @PostMapping("/master-data/district")
    APIResponseGhnDistrict getThirdPartyDistrict(
        @RequestHeader("token") String token,
        // @RequestBody Long province_id
        @RequestBody Map<String, Long> body
    );

    @PostMapping("/master-data/ward")
    APIResponseGhnWard getThirdPartyWard(
        @RequestHeader("token") String token,
        @RequestBody Map<String, Object> body
    );

    @PostMapping("/v2/shipping-order/fee")
    APIResponseGhnShippingFee getThirdPartyShippingFee(
        @RequestHeader("token") String token,
        @RequestHeader("shopId") Long shopId,
        @RequestBody Map<String, Object> body
    );
    
    @PostMapping("/v2/shipping-order/leadtime")
    APIResponseGhnLeadTime getThirdPartyLeadTime(
        @RequestHeader("token") String token,
        @RequestHeader("shopId") Long shopId,
        @RequestBody Map<String, Object> body
    );
}
