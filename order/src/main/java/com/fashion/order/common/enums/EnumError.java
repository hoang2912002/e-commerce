package com.fashion.order.common.enums;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumError {

    //----------------Auth-------------------
    ORDER_USER_INVALID_REFRESH_TOKEN("ORDER-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    ORDER_USER_INVALID_ACCESS_TOKEN("ORDER-USER-INVALID-ACCESS-TOKEN","User already with invalid Access token:",HttpStatus.BAD_REQUEST),

    //----------------Permission-------------------
    ORDER_PERMISSION_DATA_EXISTED_APIPATH("ORDER-PERMISSION-DTE-APIPATH","Permission already exists with the given Api path:",HttpStatus.CONFLICT),
    ORDER_PERMISSION_DATA_EXISTED_APIPATH_METHOD_SERVICE("ORDER-PERMISSION-DTE-APIPATH-METHOD-SERVICE","Permission already exists with the given Api path, Method, Service:",HttpStatus.CONFLICT),
    ORDER_PERMISSION_ERR_NOT_FOUND_ID("ORDER-PERMISSION-ERR-NOT-FOUND-ID","Not found permission with id:",HttpStatus.BAD_REQUEST),
    ORDER_PERMISSION_ACCESS_DENIED("ORDER-PERMISSION-ACCESS-DENIED","You do not have permission to access this resource.",HttpStatus.FORBIDDEN),

    //----------------Coupon-------------------
    ORDER_COUPON_ERR_NOT_FOUND_ID("ORDER-COUPON-CATE_NF","Not found coupon with id:",HttpStatus.CONFLICT),
    ORDER_COUPON_DATA_EXISTED_CODE("ORDER-COUPON-DTE-CODE","Coupon already exists with the given Code:",HttpStatus.CONFLICT),
    ORDER_COUPON_DATA_OUT_STOCK("ORDER-COUPON-DATA-OUT-STOCK","Coupon stock not available to operation change quantity:",HttpStatus.CONFLICT),
    ORDER_COUPON_INVALID_SIMILAR_VERSION("ORDER-COUPON-INVALID-SIMILAR-VERSION","The coupon version is not similar to the given Version:",HttpStatus.CONFLICT),
    
    //----------------Product sku-------------------
    ORDER_PRODUCT_SKU_ERR_NOT_FOUND_ID("ORDER-PRODUCT-SKU-ERR-NOT-FOUND-ID","Not found product sku with id:",HttpStatus.BAD_REQUEST),

    //----------------Order-------------------
    ORDER_ORDER_ERR_NOT_FOUND_ID("ORDER-ORDER-CATE_NF","Not found order with id:",HttpStatus.BAD_REQUEST),
    ORDER_ORDER_ERR_NOT_FOUND_CODE("ORDER-ORDER-CATE_NF_CODE","Not found order with code:",HttpStatus.BAD_REQUEST),
    ORDER_ORDER_INVALID_STATUS_TRANSITION("ORDER-ORDER-INVALID-STATUS","Invalid status transition from {currentStatus} to {newStatus}.", HttpStatus.BAD_REQUEST),
    ORDER_ORDER_CANCEL_REASON_REQUIRED("ORDER-ORDER-CANCEL-REASON-REQUIRED", "Order with CANCELLED status must have reason", HttpStatus.BAD_REQUEST),
    ORDER_ORDER_RETURNED_REASON_REQUIRED("ORDER-ORDER-RETURNED-REASON-REQUIRED", "Order with RETURNED status must have reason", HttpStatus.BAD_REQUEST),
    ORDER_ORDER_STATUS_PENDING_CANNOT_UPDATE("ORDER-ORDER-STATUS-PENDING-CANNOT-UPDATE","The order data should not be updated with the status as PENDING.",HttpStatus.BAD_REQUEST), 
    ORDER_ORDER_STATUS_CONFIRMED_CANNOT_UPDATE("ORDER-ORDER-STATUS-CONFIRMED-CANNOT-UPDATE","The order data should not be updated with the status as CONFIRMED.",HttpStatus.BAD_REQUEST), 
    ORDER_ORDER_STATUS_SHIPPING_CANNOT_UPDATE("ORDER-ORDER-STATUS-SHIPPING-CANNOT-UPDATE","The order data should not be updated with the status as SHIPPING.",HttpStatus.BAD_REQUEST), 
    ORDER_ORDER_STATUS_DELIVERED_CANNOT_UPDATE("ORDER-ORDER-STATUS-DELIVERED-CANNOT-UPDATE","The order data should not be updated with the status as DELIVERED.",HttpStatus.BAD_REQUEST), 
    ORDER_ORDER_STATUS_CANCELLED_CANNOT_UPDATE("ORDER-ORDER-STATUS-CANCELLED-CANNOT-UPDATE","The order data should not be updated with the status as CANCELLED.",HttpStatus.BAD_REQUEST), 
    ORDER_ORDER_STATUS_RETURNED_CANNOT_UPDATE("ORDER-ORDER-STATUS-RETURNED-CANNOT-UPDATE","The order data should not be updated with the status as RETURNED.",HttpStatus.BAD_REQUEST), 
    ORDER_ORDER_FIELD_FORBIDDEN_CANNOT_UPDATE_STATUS_CONFIRMED("ORDER-ORDER-FIELD-FORBIDDEN-CANNOT-UPDATE","The order data field is prohibited from being updated when the status is Confirmed.",HttpStatus.BAD_REQUEST), 
    ORDER_ORDER_ERR_MULTI_SHOP("ORDER-ORDER-ERR-MULTI-SHOP","System does not support orders with multiple shops. Please split the order.",HttpStatus.BAD_REQUEST),
    ORDER_ORDER_INVALID_SIMILAR_VERSION("ORDER-ORDER-INVALID-SIMILAR-VERSION","The order version is not similar to the given Version:",HttpStatus.CONFLICT),


    //----------------Server-------------------
    ORDER_INTERNAL_ERROR_CALL_API("ORDER-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_VALIDATION_ERROR("ORDER-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    ORDER_INVALID_FORMAT_UUID("ORDER-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),

    //----------------Kafka-------------------
    ORDER_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("ORDER-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),
    ORDER_KAFKA_DATA_SERIALIZATION_ERROR("ORDER-KAFKA-DATA-SERIALIZATION-ERROR","Can not connect to Kafka", HttpStatus.BAD_REQUEST),
    ;

    String code;
    String defaultMessage;
    HttpStatus httpStatus;

    public static EnumError fromCode(String code) {
        for (EnumError e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown DispatchError code: " + code);
    }
}
