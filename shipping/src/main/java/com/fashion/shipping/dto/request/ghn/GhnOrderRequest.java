package com.fashion.shipping.dto.request.ghn;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnOrderRequest {
    /** Token xác thực tài khoản GHN (thường truyền qua Header) */
    @JsonProperty("token")
    String token;

    /** ShopID do GHN cấp cho khách hàng */
    @JsonProperty("shop_id")
    Integer shop_id;

    // ================= THÔNG TIN NGƯỜI GỬI =================

    /** Tên người gửi */
    @JsonProperty("from_name")
    String from_name;

    /** Số điện thoại người gửi
     *  Nếu không truyền → GHN lấy theo ShopID */
    @JsonProperty("from_phone")
    String from_phone;

    /** Địa chỉ người gửi */
    @JsonProperty("from_address")
    String from_address;

    /** Phường/Xã người gửi */
    @JsonProperty("from_ward_name")
    String from_ward_name;

    /** Quận/Huyện người gửi */
    @JsonProperty("from_district_name")
    String from_district_name;

    /** Tỉnh/Thành phố người gửi */
    @JsonProperty("from_province_name")
    String from_province_name;

    // ================= THÔNG TIN NGƯỜI NHẬN =================

    /** Tên người nhận (BẮT BUỘC) */
    @JsonProperty("to_name")
    String to_name;

    /** Số điện thoại người nhận (BẮT BUỘC) */
    @JsonProperty("to_phone")
    String to_phone;

    /** Địa chỉ giao hàng (BẮT BUỘC) */
    @JsonProperty("to_address")
    String to_address;

    /** Phường/Xã người nhận (BẮT BUỘC) */
    @JsonProperty("to_ward_name")
    String to_ward_name;

    /** Quận/Huyện người nhận (BẮT BUỘC) */
    @JsonProperty("to_district_name")
    String to_district_name;

    /** Tỉnh/Thành phố người nhận (BẮT BUỘC) */
    @JsonProperty("to_province_name")
    String to_province_name;

    // ================= THÔNG TIN TRẢ HÀNG =================

    /** Số điện thoại liên hệ khi trả hàng */
    @JsonProperty("return_phone")
    String return_phone;

    /** Địa chỉ trả hàng khi giao thất bại */
    @JsonProperty("return_address")
    String return_address;

    /** Quận/Huyện trả hàng */
    @JsonProperty("return_district_id")
    Long return_district_id;

    /** Phường/Xã trả hàng */
    @JsonProperty("return_ward_code")
    String return_ward_code;

    /** Tỉnh/Thành phố trả hàng */
    // @JsonProperty("return_province_id")
    // Long return_province_id;

    // ================= THÔNG TIN ĐƠN HÀNG =================

    /** Mã đơn hàng của hệ thống khách hàng
     *  Nếu trùng → GHN trả về đơn đã tồn tại */
    @JsonProperty("client_order_code")
    String client_order_code;

    /** Tiền thu hộ COD (tối đa 50.000.000) */
    @JsonProperty("cod_amount")
    Integer cod_amount;

    /** Nội dung hàng hóa */
    @JsonProperty("content")
    String content;

    // ================= KIỆN HÀNG (HÀNG NHẸ) =================

    /** Khối lượng đơn hàng (gram)
     *  BẮT BUỘC khi service_type_id = 2 */
    @JsonProperty("weight")
    Integer weight;

    /** Chiều dài đơn hàng (cm) */
    @JsonProperty("length")
    Integer length;

    /** Chiều rộng đơn hàng (cm) */
    @JsonProperty("width")
    Integer width;

    /** Chiều cao đơn hàng (cm) */
    @JsonProperty("height")
    Integer height;

    // ================= DỊCH VỤ VẬN CHUYỂN =================

    /** Mã bưu cục gửi hàng */
    @JsonProperty("pick_station_id")
    Integer pick_station_id;

    /** Giá trị bảo hiểm hàng hóa (tối đa 5.000.000) */
    @JsonProperty("insurance_value")
    Integer insurance_value;

    /** Mã coupon GHN */
    @JsonProperty("coupon")
    String coupon;

    /** Mã loại dịch vụ
     *  2: Hàng nhẹ
     *  5: Hàng nặng */
    @JsonProperty("service_type_id")
    Integer service_type_id;

    /** Người thanh toán phí vận chuyển
     *  1: Người gửi
     *  2: Người nhận */
    @JsonProperty("payment_type_id")
    Integer payment_type_id;

    /** Ghi chú cho tài xế */
    @JsonProperty("note")
    String note;

    /** Ghi chú bắt buộc */
    @JsonProperty("required_note")
    String required_note;

    /** Ca lấy hàng */
    @JsonProperty("pick_shift")
    List<Integer> pick_shift;

    /** Thời gian mong muốn lấy hàng (Unix timestamp – seconds) */
    @JsonProperty("pickup_time")
    Long pickup_time;

    // ================= DANH SÁCH SẢN PHẨM (HÀNG NẶNG) =================

    /** Danh sách sản phẩm
     *  BẮT BUỘC khi service_type_id = 5 */
    @JsonProperty("items")
    List<Item> items;

    /** Thu thêm tiền khi giao hàng thất bại */
    @JsonProperty("cod_failed_amount")
    Integer cod_failed_amount;

    /** Mã điểm giao hàng */
    @JsonProperty("deliver_station_id")
    Integer deliver_station_id;

    // ================= INNER CLASS =================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {

        /** Tên sản phẩm */
        @JsonProperty("name")
        String name;

        /** Mã sản phẩm */
        @JsonProperty("code")
        String code;

        /** Số lượng */
        @JsonProperty("quantity")
        Integer quantity;

        /** Giá 1 sản phẩm */
        @JsonProperty("price")
        Integer price;

        /** Chiều dài sản phẩm (cm) */
        @JsonProperty("length")
        Integer length;

        /** Chiều rộng sản phẩm (cm) */
        @JsonProperty("width")
        Integer width;

        /** Khối lượng sản phẩm (gram) */
        @JsonProperty("weight")
        Integer weight;

        /** Chiều cao sản phẩm (cm) */
        @JsonProperty("height")
        Integer height;

        /** Danh mục sản phẩm */
        @JsonProperty("category")
        Category category;
    }

    // ================= INNER CLASS =================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Category {

        /** Danh mục cấp 1 */
        @JsonProperty("level1")
        String level1;
    }
}
