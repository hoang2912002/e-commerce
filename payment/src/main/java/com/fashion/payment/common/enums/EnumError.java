package com.fashion.payment.common.enums;

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
    PAYMENT_USER_INVALID_REFRESH_TOKEN("PAYMENT-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    PAYMENT_USER_INVALID_ACCESS_TOKEN("PAYMENT-USER-INVALID-ACCESS-TOKEN","User already with invalid Access token:",HttpStatus.BAD_REQUEST),
    
    //----------------Permission-------------------
    PAYMENT_PERMISSION_DATA_EXISTED_APIPATH("PAYMENT-PERMISSION-DTE-APIPATH","Permission already exists with the given Api path:",HttpStatus.CONFLICT),
    PAYMENT_PERMISSION_DATA_EXISTED_APIPATH_METHOD_SERVICE("PAYMENT-PERMISSION-DTE-APIPATH-METHOD-SERVICE","Permission already exists with the given Api path, Method, Service:",HttpStatus.CONFLICT),
    PAYMENT_PERMISSION_ERR_NOT_FOUND_ID("PAYMENT-PERMISSION-ERR-NOT-FOUND-ID","Not found permission with id:",HttpStatus.BAD_REQUEST),
    PAYMENT_PERMISSION_ACCESS_DENIED("PAYMENT-PERMISSION-ACCESS-DENIED","You do not have permission to access this resource.",HttpStatus.FORBIDDEN),

    //----------------Payment method-------------------
    PAYMENT_PAYMENT_METHOD_DATA_EXISTED_CODE("PAYMENT-PAYMENT-METHOD-DATA-EXISTED-CODE","Payment method already existed with the given Code.", HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_METHOD_DATA_EXISTED_NAME("PAYMENT-PAYMENT-METHOD-DATA-EXISTED-NAME","Payment method already existed with the given Name.", HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_METHOD_ERR_NOT_FOUND_ID("PAYMENT-PAYMENT-METHOD-NOT-FOUND-ID","Not found payment method with id.",HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_METHOD_STATUS_NOT_MATCHING("PAYMENT-PAYMENT-METHOD-STATUS-NOT-MATCHING","Payment Method not ready for third party trading section",HttpStatus.BAD_REQUEST),

    //----------------Payment-------------------
    PAYMENT_PAYMENT_NOT_FOUND_PAYMENT_METHOD("PAYMENT-PAYMENT-METHOD-NOT-FOUND","No corresponding payment method found.", HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_ERR_NOT_FOUND_ID("PAYMENT-PAYMENT-CATE_NF","Not found payment with id:", HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_PROCESS_NOT_NULL("PAYMENT-PAYMENT-PROCESS-NOTNULL", "Payment argument be null!",HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_ERR_NOT_FOUND_ORDER_ID("PAYMENT-PAYMENT-CATE_NF-ORDER-ID","Not found Payment with Order Id:",HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_STATUS_CANNOT_UPDATE_ORDER_CONFIRMED("PAYMENT-PAYMENT-STATUS-CANNOT-UPDATE-ORDER-CONFIRMED","Payment status does not support updating the order CONFIRMED status.", HttpStatus.BAD_REQUEST),
    PAYMENT_PAYMENT_STATUS_NOT_PENDING_CANNOT_UPDATE("PAYMENT-PAYMENT-STATUS-NOT-PENDING-CANNOT-UPDATE","Payment status was SUCCESS/FAILED can not update Payment", HttpStatus.BAD_REQUEST),
    

    //----------------Server-------------------
    PAYMENT_INTERNAL_ERROR_CALL_API("PAYMENT-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_VALIDATION_ERROR("PAYMENT-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_FORMAT_UUID("PAYMENT-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),

    //----------------Kafka-------------------
    PAYMENT_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("PAYMENT-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),
    PAYMENT_KAFKA_DATA_SERIALIZATION_ERROR("PAYMENT-KAFKA-DATA-SERIALIZATION-ERROR","Can not connect to Kafka", HttpStatus.BAD_REQUEST),
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
