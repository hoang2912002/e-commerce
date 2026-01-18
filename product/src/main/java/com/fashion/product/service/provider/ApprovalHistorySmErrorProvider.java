package com.fashion.product.service.provider;

import org.springframework.stereotype.Component;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.provider.ApprovalErrorProvider;
@Component
public class ApprovalHistorySmErrorProvider implements ApprovalErrorProvider{

    @Override
    public EnumError getError(ApprovalMasterEnum status) {
        return switch(status) {
            case PENDING -> EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_PENDING_NEED_APPROVED;
            case APPROVED -> EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_APPROVED_CANNOT_ADD_HISTORY;
            case REJECTED -> EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY;
            case NEEDS_ADJUSTMENT -> EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_NEED_ADJUSTMENT_CANNOT_ADD_HISTORY;
            case ADJUSTMENT -> EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_ADJUSTMENT_CANNOT_ADD_HISTORY;
            case FINISHED_ADJUSTMENT -> EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_FINISHED_ADJUSTMENT_CANNOT_ADD_HISTORY;
            default -> EnumError.PRODUCT_INTERNAL_ERROR_CALL_API;
        };
    }

    @Override
    public String getMessageCode(ApprovalMasterEnum status) {
        return switch(status) {
            case PENDING -> "shop.management.not.update.approval.status.pending";
            case APPROVED -> "shop.management.not.update.approval.status.approved";
            case REJECTED -> "shop.management.not.update.approval.status.rejected";
            case NEEDS_ADJUSTMENT -> "shop.management.not.update.approval.status.needsAdjustment";
            case ADJUSTMENT -> "shop.management.not.update.approval.status.adjustment";
            case FINISHED_ADJUSTMENT -> "shop.management.not.update.approval.status.finishedAdjustment";
            default -> "approval.history.status.invalid.flow";
        }; 
    }
    
}
