package com.fashion.shipping.dto.response.ghn;
import java.math.BigDecimal;
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
public class GhnShippingFeeResponse {
    private BigDecimal total; //Tổng tiền dịch vụ.
    @JsonProperty("service_fee")
    private BigDecimal serviceFee; // Phí dịch vụ.
    // private BigDecimal insuranceFee; // Phí khai giá hàng hóa.
    // private BigDecimal pickStationFee; // Phí gửi hàng tại bưu cục.
    // private BigDecimal couponValue; // Giá trị khuyến mãi.
    // private BigDecimal r2sFee; // Phí giao lại hàng.
    // private BigDecimal documentReturn; // Phí giao tài liệu
    // private BigDecimal doubleCheck; // Phí đồng kiểm.
    // private BigDecimal codFee; // Phí thu tiền COD
    // private BigDecimal pick_remote_areas_fee; // Phí lấy hàng vùng xa.
    // private BigDecimal deliver_remote_areas_fee; // Phí giao hàng vùng xa.
    // private BigDecimal cod_failed_fee; // Phí thu tiền khi giao thất bại.

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class APIResponseGhnShippingFee {
        private Integer code;
        private String message;
        private GhnShippingFeeResponse data; 
    }
}
