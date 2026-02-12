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

    //----------------Auth-------------------
    PRODUCT_USER_INVALID_REFRESH_TOKEN("PRODUCT-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    PRODUCT_USER_INVALID_ACCESS_TOKEN("PRODUCT-USER-INVALID-ACCESS-TOKEN","User already with invalid Access token:",HttpStatus.BAD_REQUEST),
    
    //----------------Category-------------------
    PRODUCT_CATEGORY_DATA_EXISTED_ID("PRODUCT-CATEGORY-DATA-EXISTED-ID","Category already exists with the given Id:",HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_DATA_EXISTED_SLUG("PRODUCT-CATEGORY-DATA-EXISTED-SLUG","Category already exists with the given Slug:",HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_ERR_NOT_FOUND_ID("PRODUCT-CATEGORY-NOT-FOUND-ID","Not found category with id:",HttpStatus.BAD_REQUEST),
    PRODUCT_CATEGORY_INVALID_ID("PRODUCT-CATEGORY-INVALID-ID","Category already with invalid id:",HttpStatus.CONFLICT),

    //----------------Option-------------------
    PRODUCT_OPTION_DATA_EXISTED_NAME("PRODUCT-OPTION-DATA-EXISTED-NAME","Option already exists with the given Name:",HttpStatus.CONFLICT),
    PRODUCT_OPTION_DATA_EXISTED_SLUG("PRODUCT-OPTION-DATA-EXISTED-SLUG","Option already exists with the given Slug:",HttpStatus.CONFLICT),
    PRODUCT_OPTION_ERR_NOT_FOUND_ID("PRODUCT-OPTION-NOT-FOUND-ID","Not found option with id:",HttpStatus.BAD_REQUEST),
    
    //----------------Option value-------------------
    PRODUCT_OPTION_VALUE_DATA_EXISTED_VALUE("PRODUCT-OPTION-VALUE-DATA-EXISTED-VALUE","Option value already exists with the given Value:",HttpStatus.CONFLICT),
    PRODUCT_OPTION_VALUE_ERR_NOT_FOUND_ID("PRODUCT-OPTION-VALUE-NOT-FOUND-ID","Not found option value with id:",HttpStatus.BAD_REQUEST),

    //----------------Promotion-------------------
    PRODUCT_PROMOTION_DATA_EXISTED_CODE("PRODUCT-PROMOTION-DATA-EXISTED-CODE","Promotion already exists with the given Code:",HttpStatus.CONFLICT),
    PRODUCT_PROMOTION_ERR_NOT_FOUND_ID("PRODUCT-PROMOTION-NOT-FOUND-ID","Not found promotion with id:",HttpStatus.BAD_REQUEST),
    PRODUCT_PROMOTION_INVALID_QUANTITY("PRODUCT-PROMOTION-INVALID-QUANTITY","Failed to update promotion quantity immediately after Create/Update Order successful", HttpStatus.BAD_REQUEST),
    //----------------Product-------------------
    PRODUCT_PRODUCT_DATA_EXISTED_NAME("PRODUCT-PRODUCT-DATA-EXISTED-NAME","Product already exists with the given Name:",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_ERR_NOT_FOUND_ID("PRODUCT-PRODUCT-NOT-FOUND-ID","Not found product with id:",HttpStatus.BAD_REQUEST),
    PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_PENDING("PRODUCT-PRODUCT-DTE-APPROVAL-PENDING","Product already exists with the pending approval history request:",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_APPROVED("PRODUCT-PRODUCT-DTE-APPROVAL-APPROVED","Product already exists with the approved approval history request:",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_REJECTED("PRODUCT-PRODUCT-DTE-APPROVAL-REJECTED","Product already exists with the rejected approval history request:",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_NEEDS_ADJUSTMENT("PRODUCT-PRODUCT-DTE-APPROVAL-NEEDS-ADJUSTMENT","Product already exists with the need adjustment approval history request:",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_ADJUSTMENT("PRODUCT-PRODUCT-DTE-APPROVAL-ADJUSTMENT","Product already exists with the adjustment approval history request:",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_FINISHED_ADJUSTMENT("PRODUCT-PRODUCT-DTE-APPROVAL-FINISHED-ADJUSTMENT","Product already exists with the finished adjustment approval history request:",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_ERR_NOT_FOUND_SHOP_MANAGEMENT("PRODUCT-PRODUCT-NOT-FOUND-SHOP-MANAGEMENT","The user managing this shop management has no product",HttpStatus.CONFLICT),
    PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_PENDING_ADJUSTMENT("PRODUCT-PRODUCT-DTE-APPROVAL-PENDING-ADJUSTMENT-CANNOT-UPDATE","Product already exists with the adjustment pending/approval history.",HttpStatus.CONFLICT),

    //----------------Product sku-------------------
    PRODUCT_PRODUCT_SKU_ERR_NOT_FOUND_ID("PRODUCT-PRODUCT-SKU-ERR-NOT-FOUND-ID","Not found product sku with id:",HttpStatus.BAD_REQUEST),

    //----------------ShopManagement-------------------
    PRODUCT_SHOP_MANAGEMENT_DATA_EXISTED_NAME("PRODUCT-SHOP-MANAGEMENT-DATA-EXISTED-NAME","Shop management already exists with Name", HttpStatus.CONFLICT),
    PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID("PRODUCT-SHOP-MANAGEMENT-NOT-FOUND-ID","Not found shop management with id:",HttpStatus.CONFLICT),
    PRODUCT_SHOP_MANAGEMENT_DATA_EXISTED_APPROVAL_PENDING("PRODUCT-SHOP-MANAGEMENT-DTE-APPROVAL-PENDING","Shop management already exists with the pending approval request:",HttpStatus.CONFLICT),
    PRODUCT_SHOP_MANAGEMENT_HOST_NOT_MATCHING("PRODUCT-SHOP-MANAGEMENT-HOST-NOT-MATCHING","No shop managements found owned by user",HttpStatus.CONFLICT),

    //----------------Permission-------------------
    PRODUCT_PERMISSION_DATA_EXISTED_APIPATH("PRODUCT-PERMISSION-DTE-APIPATH","Permission already exists with the given Api path:",HttpStatus.CONFLICT),
    PRODUCT_PERMISSION_DATA_EXISTED_APIPATH_METHOD_SERVICE("PRODUCT-PERMISSION-DTE-APIPATH-METHOD-SERVICE","Permission already exists with the given Api path, Method, Service:",HttpStatus.CONFLICT),
    PRODUCT_PERMISSION_ERR_NOT_FOUND_ID("PRODUCT-PERMISSION-ERR-NOT-FOUND-ID","Not found permission with id:",HttpStatus.BAD_REQUEST),
    PRODUCT_PERMISSION_ACCESS_DENIED("PRODUCT-PERMISSION-ACCESS-DENIED","You do not have permission to access this resource.",HttpStatus.FORBIDDEN),

    //----------------ApprovalMaster-------------------
    PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ID("PRODUCT-APPROVAL-MASTER-NOT-FOUND-ID","Not found approval master with id:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_EXISTED_ENTITY_TYPE_STATUS_STEP("PRODUCT-APPROVAL-MASTER-DATA-EXISTED-ENTITY-TYPE-STATUS-STEP","Approval master already exists with the given EntityType, Status, and Step:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY("PRODUCT-APPROVAL-MASTER-STATUS-REJECTED-CANNOT-ADD-HISTORY","Cannot add approval history to an approval master with REJECTED status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_PENDING_CANNOT_ADD_HISTORY("PRODUCT-APPROVAL-MASTER-STATUS-PENDING-CANNOT-ADD-HISTORY","Cannot add approval history to an approval master with PENDING status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_PENDING_NEED_APPROVED("PRODUCT-APPROVAL-MASTER-STATUS-PENDING-NEED-APPROVED","Cannot operate with approval history with PENDING status, please wait for approval",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_ADJUSTMENT_NEED_APPROVED("PRODUCT-APPROVAL-MASTER-STATUS-ADJUSTMENT-NEED-APPROVED","Cannot operate with approval history with ADJUSTMENT status, please wait for approval",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_APPROVED_CANNOT_ADD_HISTORY("PRODUCT-APPROVAL-MASTER-STATUS-APPROVED-CANNOT-ADD-HISTORY","Cannot add approval history to an approval master with APPROVED status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_NEED_ADJUSTMENT_CANNOT_ADD_HISTORY("PRODUCT-APPROVAL-MASTER-STATUS-NEED-ADJUSTMENT-CANNOT-ADD-HISTORY","Cannot add approval history to an approval master with NEED ADJUSTMENT status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_ADJUSTMENT_CANNOT_ADD_HISTORY("PRODUCT-APPROVAL-MASTER-STATUS-ADJUSTMENT-CANNOT-ADD-HISTORY","Cannot add approval history to an approval master with ADJUSTMENT status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_DATA_STATUS_FINISHED_ADJUSTMENT_CANNOT_ADD_HISTORY("PRODUCT-APPROVAL-MASTER-STATUS-FINISHED-ADJUSTMENT-CANNOT-ADD-HISTORY","Cannot add approval history to an approval master with FINISHED ADJUSTMENT status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ENTITY_TYPE_STATUS("PRODUCT-APPROVAL-MASTER-NOT-FOUND-ENTITY-TYPE-STATUS","Not found approval master with EntityType and Status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ENTITY_STATUS("PRODUCT-APPROVAL-MASTER-NOT-FOUND-ENTITY-STATUS","Not found approval master with EntityType and Status:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ENTITY("PRODUCT-APPROVAL-MASTER-NOT-FOUND-ENTITY","Not found approval master with EntityType:",HttpStatus.CONFLICT),

    //----------------ApprovalHistory-------------------
    PRODUCT_APPROVAL_HISTORY_ERR_NOT_FOUND_LAST_PRODUCT("PRODUCT-APPROVAL-HISTORY-NOT-FOUND-LAST-PRODUCT","Not found approval history with product id:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_HISTORY_ERR_NOT_FOUND_ID("PRODUCT-APPROVAL-HISTORY-NOT-FOUND-ID","Not found approval history with id:",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_HISTORY_ERR_NOT_FOUND_LAST_SHOP_MANAGEMENT("PRODUCT-APPROVAL-HISTORY-NOT-FOUND-LAST-SHOP-MANAGEMENT","The Shop management does not have an approval history, please submit it with PENDING status.",HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_HISTORY_CURRENT_NOT_LATEST("PRODUCT-APPROVAL-HISTORY-CURRENT-NOT-LATEST","The approval history is not the latest.", HttpStatus.CONFLICT),
    PRODUCT_APPROVAL_HISTORY_STATUS_CANNOT_BE_CHANGED("PRODUCT-APPROVAL-HISTORY-STATUS-CANNOT-BE-CHANGED", "It is not possible to change the approval status in the approval history.", HttpStatus.BAD_REQUEST),

    //----------------Server-------------------
    PRODUCT_INTERNAL_ERROR_CALL_API("PRODUCT-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_VALIDATION_ERROR("PRODUCT-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    PRODUCT_INVALID_FORMAT_UUID("PRODUCT-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),
    PRODUCT_VERSION_CACHE("PRODUCT-VERSION-CACHE", "Request query must has version", HttpStatus.BAD_REQUEST),

    //----------------Kafka-------------------
    PRODUCT_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("PRODUCT-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),
    PRODUCT_KAFKA_SAGA_COMPLETED_MESSAGE_ERROR("PRODUCT-KAFKA-SAGA-COMPLETED-MESSAGE-ERROR", "Unable to handle successful saga completion state", HttpStatus.BAD_REQUEST),
    PRODUCT_KAFKA_SAGA_FAILED_MESSAGE_ERROR("PRODUCT-KAFKA-SAGA-FAILED-MESSAGE-ERROR", "Unable to handle successful saga completion state", HttpStatus.BAD_REQUEST),
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
