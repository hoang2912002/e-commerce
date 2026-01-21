package com.fashion.product.service.provider;

import org.springframework.stereotype.Component;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.provider.ApprovalErrorProvider;

@Component
public class ApprovalHistoryInventoryErrorProvider implements ApprovalErrorProvider{
    @Override
    public EnumError getError(ApprovalMasterEnum status) {
        return switch(status) {
            case PENDING -> EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_PENDING;
            case APPROVED -> EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_APPROVED;
            case REJECTED -> EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_REJECTED;
            case NEEDS_ADJUSTMENT -> EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_NEEDS_ADJUSTMENT;
            case ADJUSTMENT -> EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_ADJUSTMENT;
            case FINISHED_ADJUSTMENT -> EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_FINISHED_ADJUSTMENT;
        };
    }

    @Override
    public String getMessageCode(ApprovalMasterEnum status) {
       return switch(status) {
            case PENDING -> "approval.history.last.pending.not.create.update.inventory";
            case APPROVED -> "approval.history.last.approved.not.create.update.inventory";
            case REJECTED -> "approval.history.last.rejected.not.create.update.inventory";
            case NEEDS_ADJUSTMENT -> "approval.history.last.needsAdjustment.not.create.update.inventory";
            case ADJUSTMENT -> "approval.history.last.adjustment.not.create.update.inventory";
            case FINISHED_ADJUSTMENT -> "approval.history.last.finishedAdjustment.not.create.update.inventory";
        }; 
    }
    
}
