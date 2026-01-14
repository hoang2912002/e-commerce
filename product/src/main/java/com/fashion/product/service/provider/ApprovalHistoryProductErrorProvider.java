package com.fashion.product.service.provider;

import org.springframework.stereotype.Component;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.provider.ApprovalErrorProvider;
@Component
public class ApprovalHistoryProductErrorProvider implements ApprovalErrorProvider{
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
            case PENDING -> "product.data.existed.approval.pending";
            case APPROVED -> "product.data.existed.approval.approved";
            case REJECTED -> "product.data.existed.approval.rejected";
            case NEEDS_ADJUSTMENT -> "product.data.existed.approval.needsAdjustment";
            case ADJUSTMENT -> "product.data.existed.approval.adjustment";
            case FINISHED_ADJUSTMENT -> "product.data.existed.approval.finishedAdjustment";
        }; 
    }
    
}
