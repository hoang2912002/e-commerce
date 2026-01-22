package com.fashion.order.common.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fashion.order.exception.ServiceException;

public enum ApprovalMasterEnum {
    PENDING, 
    APPROVED, 
    REJECTED, 
    ADJUSTMENT,           // Chỉnh sửa tồn kho
    NEEDS_ADJUSTMENT,    // Đề xuất điều chỉnh
    FINISHED_ADJUSTMENT, // Hoàn tất điều chỉnh
    ;
}
