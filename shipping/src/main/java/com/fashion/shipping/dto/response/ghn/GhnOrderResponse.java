package com.fashion.shipping.dto.response.ghn;
import java.time.Instant;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class GhnOrderResponse {
    // Mã đơn hàng GHN
    @JsonProperty("order_code")
    private String orderCode;

    // Mã phân loại đơn hàng
    @JsonProperty("sort_code")
    private String sortCode;

    // Loại vận chuyển (vd: truck)
    @JsonProperty("trans_type")
    private String transType;

    // Mã phường/xã (có thể rỗng)
    @JsonProperty("ward_encode")
    private String wardEncode;

    // Mã quận/huyện (có thể rỗng)
    @JsonProperty("district_encode")
    private String districtEncode;

    // Thông tin phí vận chuyển
    @JsonProperty("fee")
    private Fee fee;

    // Tổng phí dịch vụ
    @JsonProperty("total_fee")
    private Integer totalFee;

    // Thời gian giao dự kiến (ISO-8601, UTC) Ví dụ: 2020-06-03T16:00:00Z
    @JsonProperty("expected_delivery_time")
    private Instant expectedDeliveryTime;


    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class Fee {
        // Phí vận chuyển chính
        @JsonProperty("main_service")
        private Integer mainService;

        // Phí khai giá / bảo hiểm hàng hóa
        @JsonProperty("insurance")
        private Integer insurance;

        // Phí gửi hàng tại bưu cục
        @JsonProperty("station_do")
        private Integer stationDo;

        // Phí lấy hàng tại bưu cục
        @JsonProperty("station_pu")
        private Integer stationPu;

        // Phí hoàn hàng
        @JsonProperty("return")
        private Integer returnFee;

        // Phí giao lại hàng
        @JsonProperty("r2s")
        private Integer r2s;

        // Giá trị khuyến mãi
        @JsonProperty("coupon")
        private Integer coupon;

        // Phí phát sinh khi giao COD thất bại
        @JsonProperty("cod_failed_fee")
        private Integer codFailedFee;
    }

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class APIResponseGhnOrder {
        private Integer code;
        private String message;
        private GhnOrderResponse data; 
    }
}
