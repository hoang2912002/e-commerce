package com.fashion.product.common.enums;

public enum ApprovalMasterEnum {
    PENDING, 
    APPROVED, 
    REJECTED, 
    CANCELLED,
    ADJUSTMENT,           // Chỉnh sửa tồn kho
    NEEDS_ADJUSTMENT,    // Đề xuất điều chỉnh
    FINISHED_ADJUSTMENT, // Hoàn tất điều chỉnh
}
