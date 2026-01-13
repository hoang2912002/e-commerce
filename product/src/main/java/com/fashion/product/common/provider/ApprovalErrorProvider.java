package com.fashion.product.common.provider;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;

public interface ApprovalErrorProvider {
    // EnumError getPendingError();
    // EnumError getApprovedError();
    // EnumError getRejectedError();
    // EnumError getNeedAdjustmentError();
    // EnumError getAdjustmentError();
    // EnumError getFinishedAdjustmentError();

    EnumError getError(ApprovalMasterEnum status);
    String getMessageCode(ApprovalMasterEnum status);
}
