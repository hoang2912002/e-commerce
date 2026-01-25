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

    //----------------Permission-------------------
    INVENTORY_PERMISSION_DATA_EXISTED_APIPATH("INVENTORY-PERMISSION-DTE-APIPATH","Permission already exists with the given Api path:",HttpStatus.CONFLICT),
    INVENTORY_PERMISSION_DATA_EXISTED_APIPATH_METHOD_SERVICE("INVENTORY-PERMISSION-DTE-APIPATH-METHOD-SERVICE","Permission already exists with the given Api path, Method, Service:",HttpStatus.CONFLICT),
    INVENTORY_PERMISSION_ERR_NOT_FOUND_ID("INVENTORY-PERMISSION-ERR-NOT-FOUND-ID","Not found permission with id:",HttpStatus.BAD_REQUEST),
    INVENTORY_PERMISSION_ACCESS_DENIED("INVENTORY-PERMISSION-ACCESS-DENIED","You do not have permission to access this resource.",HttpStatus.FORBIDDEN),


    //----------------WareHouse-------------------
    INVENTORY_WARE_HOUSE_DATA_EXISTED_NAME("INVENTORY-INVENTORY-DATA-EXISTED-NAME","Ware house already exists with the given Name:",HttpStatus.CONFLICT),
    INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID("INVENTORY-INVENTORY-NOT-FOUND-ID","Not found ware house with id:",HttpStatus.BAD_REQUEST),
    INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_STATUS("INVENTORY-INVENTORY-NOT-FOUND-STATUS","Not found ware house with Status:",HttpStatus.BAD_REQUEST),
    INVENTORY_WARE_HOUSE_DATA_EXISTED_CODE("INVENTORY-INVENTORY-DATA-EXISTED-CODE","Ware house already exists with the given Code:",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_PENDING_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-PENDING-CANNOT-CREATE-UPDATE-HISTORY","Data status is pending, cannot create or update history",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_INACTIVE_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-INACTIVE-CANNOT-CREATE-UPDATE-HISTORY","Data status is inactive, cannot create or update history",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_CLOSED_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-CLOSED-CANNOT-CREATE-UPDATE-HISTORY","Data status is closed, cannot create or update history",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_ACTIVE_CANNOT_CREATE_UPDATE_HISTORY("INVENTORY-DATA-STATUS-ACTIVE-CANNOT-CREATE-UPDATE-HISTORY","Data status is active, cannot create or update history",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_PENDING_STATUS_CHANGE_MUST_BE_ACTIVE_CLOSED("INVENTORY-DATA-STATUS-PENDING-STATUS-CHANGE-MUST-BE-ACTIVE-CLOSED","Data status is pending, status change must be active or closed",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_INACTIVE_STATUS_CHANGE_MUST_BE_ACTIVE_CLOSED("INVENTORY-DATA-STATUS-INACTIVE-STATUS-CHANGE-MUST-BE-ACTIVE-CLOSED","Data status is inactive, status change must be active or closed",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_ACTIVE_STATUS_CHANGE_MUST_BE_INACTIVE_CLOSED("INVENTORY-DATA-STATUS-ACTIVE-STATUS-CHANGE-MUST-BE-INACTIVE-CLOSED","Data status is active, status change must be inactive or closed",HttpStatus.CONFLICT),
    INVENTORY_DATA_STATUS_CLOSED_STATUS_CHANGE_NOT_ALLOWED("INVENTORY-DATA-STATUS-CLOSED-STATUS-CHANGE-NOT-ALLOWED","Data status is closed, status change is not allowed",HttpStatus.CONFLICT),

    //----------------Inventory-------------------
    INVENTORY_INVENTORY_ERR_NOT_FOUND_ID("INVENTORY-INVENTORY-CATE_NF","Not found inventory with id:",HttpStatus.CONFLICT),
    INVENTORY_INVENTORY_DATA_EXISTED_CODE("INVENTORY-INVENTORY-DTE-CODE","Inventory already exists with the given Code:",HttpStatus.CONFLICT),
    INVENTORY_INVENTORY_DATA_EXISTED_PRODUCT_ID( "INVENTORY-INVENTORY-DTE-PRODUCT-ID","Inventory already exists with the given Product ID:",HttpStatus.CONFLICT),
    INVENTORY_INVENTORY_ERR_NOT_FOUND_PRODUCT_ID( "INVENTORY-INVENTORY-CATE_NF-PRODUCT-ID","Not found inventory with Product ID:",HttpStatus.CONFLICT),
    INVENTORY_INVENTORY_ERR_NOT_FOUND_PRODUCT_SKU_ID( "INVENTORY-INVENTORY-CATE_NF-PRODUCT-SKU-ID","Not found inventory with Product sku ID:",HttpStatus.CONFLICT),
    INVENTORY_INVENTORY_ERR_NOT_UPDATE_STATUS_APPROVED("INVENTORY-INVENTORY-ERR-NOT-UPDATE-STATUS-APPROVED","Cannot update inventory when product is in APPROVED status:",HttpStatus.CONFLICT),
    INVENTORY_INVENTORY_INVALID_QUANTITY_AVAILABLE("INVENTORY-INVENTORY-INVALID-QUANTITY-AVAILABLE","Inventory quantity available must not be negative number", HttpStatus.CONFLICT),
    INVENTORY_INVENTORY_ERR_NOT_FOUND_PRODUCT_PRODUCT_SKU_WARE_HOUSE("INVENTORY-INVENTORY-ERR-NOT-FOUND-PRODUCT-PRODUCT-SKU-WARE-HOUSE","Not found inventory with product, product sku, ware house",HttpStatus.BAD_REQUEST),

    //----------------Product sku-------------------
    INVENTORY_PRODUCT_SKU_ERR_NOT_FOUND_ID("INVENTORY-PRODUCT-SKU-ERR-NOT-FOUND-ID","Not found product sku with id:",HttpStatus.BAD_REQUEST),

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
