package com.fashion.product.common.enums;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumError {

    // Auth
    PRODUCT_USER_INVALID_REFRESH_TOKEN("PRODUCT-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    
    // Category
    PRODUCT_CATEGORY_DATA_EXISTED_ID("PRODUCT-CATEGORY-DATA-EXISTED-ID","Category already exists with the given Id:",HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_DATA_EXISTED_SLUG("PRODUCT-CATEGORY-DATA-EXISTED-SLUG","Category already exists with the given Slug:",HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_ERR_NOT_FOUND_ID("PRODUCT-CATEGORY-NOT-FOUND-ID","Not found category with id:",HttpStatus.BAD_REQUEST),

    //----------------Option-------------------
    PRODUCT_OPTION_DATA_EXISTED_NAME("PRODUCT-OPTION-DATA-EXISTED-NAME","Option already exists with the given Name:",HttpStatus.CONFLICT),
    PRODUCT_OPTION_DATA_EXISTED_SLUG("PRODUCT-OPTION-DATA-EXISTED-SLUG","Option already exists with the given Slug:",HttpStatus.CONFLICT),
    PRODUCT_OPTION_ERR_NOT_FOUND_ID("PRODUCT-OPTION-NOT-FOUND-ID","Not found option with id:",HttpStatus.BAD_REQUEST),
    
    //----------------Option value-------------------
    PRODUCT_OPTION_VALUE_DATA_EXISTED_VALUE("PRODUCT-OPTION-VALUE-DATA-EXISTED-VALUE","Option value already exists with the given Value:",HttpStatus.CONFLICT),
    PRODUCT_OPTION_VALUE_ERR_NOT_FOUND_ID("PRODUCT-OPTION-VALUE-NOT-FOUND-ID","Not found option value with id:",HttpStatus.BAD_REQUEST),


    // Server
    PRODUCT_INTERNAL_ERROR_CALL_API("PRODUCT-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_VALIDATION_ERROR("PRODUCT-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    PRODUCT_INVALID_FORMAT_UUID("PRODUCT-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),

    // Kafka
    PRODUCT_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("PRODUCT-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),
    PRODUCT_KAFKA_DATA_SERIALIZATION_ERROR("PRODUCT-KAFKA-DATA-SERIALIZATION-ERROR","Can not connect to Kafka", HttpStatus.BAD_REQUEST),
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
