package com.fashion.shipping.common.enums;

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
    SHIPPING_USER_INVALID_REFRESH_TOKEN("PRODUCT-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    SHIPPING_USER_INVALID_ACCESS_TOKEN("PRODUCT-USER-INVALID-ACCESS-TOKEN","User already with invalid Access token:",HttpStatus.BAD_REQUEST),
    
    //----------------Shipping-------------------
    SHIPPING_SHIPPING_ERR_NOT_FOUND_ID("SHIPPING-SHIPPING-ERR-NOT-FOUND-ID", "Not found Shipping with Id:", HttpStatus.BAD_REQUEST),
    SHIPPING_SHIPPING_ERR_NOT_FOUND_ORDER_ID("SHIPPING-SHIPPING-ERR-NOT-FOUND-ORDER-ID", "Not found Shipping with Order Id:", HttpStatus.BAD_REQUEST),
    SHIPPING_SHIPPING_PROVIDER_NOT_SUPPORTED("SHIPPING-SHIPPING-PROVIDER-NOT-SUPPORTED", "Currently this provider shipping not support:", HttpStatus.BAD_REQUEST),
    SHIPPING_SHIPPING_CALL_THIRD_API_ERROR("SHIPPING-SHIPPING-CALL-THIRD-API-ERROR","Failed to call third api", HttpStatus.BAD_REQUEST),
    //----------------Server-------------------
    SHIPPING_INTERNAL_ERROR_CALL_API("PRODUCT-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    SHIPPING_VALIDATION_ERROR("PRODUCT-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    SHIPPING_INVALID_FORMAT_UUID("PRODUCT-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),
    SHIPPING_VERSION_CACHE("PRODUCT-VERSION-CACHE", "Request query must has version", HttpStatus.BAD_REQUEST),

    //----------------Kafka-------------------
    SHIPPING_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("PRODUCT-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),
    SHIPPING_KAFKA_SAGA_COMPLETED_MESSAGE_ERROR("PRODUCT-KAFKA-SAGA-COMPLETED-MESSAGE-ERROR", "Unable to handle successful saga completion state", HttpStatus.BAD_REQUEST),
    SHIPPING_KAFKA_SAGA_FAILED_MESSAGE_ERROR("PRODUCT-KAFKA-SAGA-FAILED-MESSAGE-ERROR", "Unable to handle successful saga completion state", HttpStatus.BAD_REQUEST),
    SHIPPING_KAFKA_DATA_SERIALIZATION_ERROR("PRODUCT-KAFKA-DATA-SERIALIZATION-ERROR","Can not connect to Kafka", HttpStatus.BAD_REQUEST),
    
    // Resilience4j
    IDENTITY_RESILIENCE4J_CIRCUIT_BREAKER_OPEN("IDENTITY-RESILIENCE4J-CIRCUIT-BREAKER-OPEN", "Service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE),
    IDENTITY_RESILIENCE4J_RATE_LIMITER("IDENTITY-RESILIENCE4J-RATE-LIMITER", "Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS),
    IDENTITY_RESILIENCE4J_RETRY("IDENTITY-RESILIENCE4J-RETRY", "Service is currently unavailable. Retrying...", HttpStatus.SERVICE_UNAVAILABLE),
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
