package com.fashion.product.common.enums;

import java.util.Map;

import com.fashion.product.common.provider.ApprovalErrorProvider;
import com.fashion.product.exception.ServiceException;

public enum ApprovalMasterEnum {
    PENDING, 
    APPROVED, 
    REJECTED, 
    ADJUSTMENT,           // Chỉnh sửa tồn kho
    NEEDS_ADJUSTMENT,    // Đề xuất điều chỉnh
    FINISHED_ADJUSTMENT, // Hoàn tất điều chỉnh
    ;
    public void validateTransition(ApprovalMasterEnum status, ApprovalErrorProvider errorProvider, Map<String, Object> params){
        boolean isValid = switch (this) {
            case PENDING -> status == APPROVED || status == REJECTED;
            case APPROVED, REJECTED -> status == NEEDS_ADJUSTMENT;
            case NEEDS_ADJUSTMENT -> status == ADJUSTMENT;
            case ADJUSTMENT -> status == FINISHED_ADJUSTMENT;
            case FINISHED_ADJUSTMENT -> status == APPROVED;
        };

        if (!isValid) {
            throw new ServiceException(
                errorProvider.getError(this),
                errorProvider.getMessageCode(this),
                params
            );
        }
    }
}
