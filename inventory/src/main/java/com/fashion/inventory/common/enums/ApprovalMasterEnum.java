package com.fashion.inventory.common.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// import com.fashion.inventory.common.provider.ApprovalErrorProvider;
import com.fashion.inventory.exception.ServiceException;

public enum ApprovalMasterEnum {
    PENDING, 
    APPROVED, 
    REJECTED, 
    ADJUSTMENT,           // Chỉnh sửa tồn kho
    NEEDS_ADJUSTMENT,    // Đề xuất điều chỉnh
    FINISHED_ADJUSTMENT, // Hoàn tất điều chỉnh
    ;

    // // Create update approval
    // public void validateTransition(ApprovalMasterEnum status, ApprovalErrorProvider errorProvider, Map<String, Object> params){
    //     boolean isValid = switch (this) {
    //         case PENDING -> status == APPROVED || status == REJECTED;
    //         case APPROVED, REJECTED -> status == NEEDS_ADJUSTMENT;
    //         case NEEDS_ADJUSTMENT -> status == ADJUSTMENT;
    //         case ADJUSTMENT -> status == FINISHED_ADJUSTMENT;
    //         case FINISHED_ADJUSTMENT -> status == APPROVED;
    //     };

    //     if (!isValid) {
    //         throw new ServiceException(
    //             errorProvider.getError(this),
    //             errorProvider.getMessageCode(this),
    //             params
    //         );
    //     }
    // }

    // // Product
    // public void validateCreateAbility(ApprovalErrorProvider errorProvider, Map<String, Object> params){
    //     Set<ApprovalMasterEnum> listExistMasterEnums = Set.of(ApprovalMasterEnum.values());

    //     boolean isValid = listExistMasterEnums.contains(this);
    //     if (!isValid) {
    //         throw new ServiceException(
    //             errorProvider.getError(this),
    //             errorProvider.getMessageCode(this),
    //             params
    //         );
    //     }
    // }

    // // Product
    // public void validateUpdateAbility(UUID requestId) {
    //     // Chỉ PENDING và ADJUSTMENT là được phép cập nhật dữ liệu
    //     if (this != PENDING && this != ADJUSTMENT) {
    //         throw new ServiceException(
    //             EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_PENDING_ADJUSTMENT, 
    //             "inventory.not.update.approval.status.pending.approved",
    //             Map.of("ID", requestId)
    //         );
    //     }
    // }

    // //Shop management
    // public void validateCrUpAbility(ApprovalMasterEnum nextStatus, ApprovalErrorProvider errorProvider, Map<String, Object> params){
        
    //     boolean isValid = switch (this) {
    //         case PENDING -> nextStatus == null || nextStatus == PENDING;
    //         case APPROVED -> nextStatus == null;
    //         case NEEDS_ADJUSTMENT, FINISHED_ADJUSTMENT, REJECTED -> false;
    //         case ADJUSTMENT -> nextStatus == FINISHED_ADJUSTMENT;
    //     };
    //     if (!isValid) {
    //         throw new ServiceException(
    //             errorProvider.getError(this),
    //             errorProvider.getMessageCode(this),
    //             params
    //         );
    //     }
    // }
}
