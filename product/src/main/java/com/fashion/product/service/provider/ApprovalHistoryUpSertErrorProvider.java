package com.fashion.product.service.provider;

import org.springframework.stereotype.Component;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.provider.ApprovalErrorProvider;
@Component
public class ApprovalHistoryUpSertErrorProvider implements ApprovalErrorProvider{
    @Override
    public EnumError getError(ApprovalMasterEnum status) {
        return switch(status) {
            case PENDING -> EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_PENDING_CANNOT_ADD_HISTORY;
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
            case PENDING -> "approval.history.last.pending.current.not.approved";
            case APPROVED -> "approval.history.last.approved.current.not.needsAdjustment";
            case REJECTED -> "approval.history.last.rejected.current.not.needsAdjustment";
            case NEEDS_ADJUSTMENT -> "approval.history.last.needsAdjustment.current.not.adjustment";
            case ADJUSTMENT -> "approval.history.last.adjustment.current.not.finishedAdjustment";
            case FINISHED_ADJUSTMENT -> "approval.history.last.finishedAdjustment.current.not.approved";
            default -> "approval.history.status.invalid.flow";
        }; 
    }
    
}
