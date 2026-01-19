package com.fashion.inventory.common.enums;

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
    INVENTORY_USER_INVALID_REFRESH_TOKEN("INVENTORY-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    INVENTORY_USER_INVALID_ACCESS_TOKEN("INVENTORY-USER-INVALID-ACCESS-TOKEN","User already with invalid Access token:",HttpStatus.BAD_REQUEST),

    //----------------WareHouse-------------------
    INVENTORY_WARE_HOUSE_DATA_EXISTED_NAME("INVENTORY-INVENTORY-DATA-EXISTED-NAME","Ware house already exists with the given Name:",HttpStatus.CONFLICT),
    INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID("INVENTORY-INVENTORY-NOT-FOUND-ID","Not found ware house with id:",HttpStatus.BAD_REQUEST),
    INVENTORY_WARE_HOUSE_DATA_EXISTED_CODE("INVENTORY-INVENTORY-DATA-EXISTED-CODE","Ware house already exists with the given Code:",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_PENDING_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-PENDING-CANNOT-CREATE-UPDATE-HISTORY","Data status is pending, cannot create or update history",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_INACTIVE_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-INACTIVE-CANNOT-CREATE-UPDATE-HISTORY","Data status is inactive, cannot create or update history",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_CLOSED_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-CLOSED-CANNOT-CREATE-UPDATE-HISTORY","Data status is closed, cannot create or update history",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_ACTIVE_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-ACTIVE-CANNOT-CREATE-UPDATE-HISTORY","Data status is active, cannot create or update history",HttpStatus.CONFLICT),

    //----------------Server-------------------
    INVENTORY_INTERNAL_ERROR_CALL_API("INVENTORY-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVENTORY_VALIDATION_ERROR("INVENTORY-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    INVENTORY_INVALID_FORMAT_UUID("INVENTORY-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),

    //----------------Kafka-------------------
    INVENTORY_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("INVENTORY-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),
    INVENTORY_KAFKA_DATA_SERIALIZATION_ERROR("INVENTORY-KAFKA-DATA-SERIALIZATION-ERROR","Can not connect to Kafka", HttpStatus.BAD_REQUEST),
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
