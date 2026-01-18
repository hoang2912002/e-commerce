package com.fashion.inventory.common.enums;

public enum InventoryTransactionTypeEnum {
    IMPORT,         // nhập kho
    EXPORT,         // xuất kho
    ORDER_RESERVE,  // giữ hàng khi khách đặt
    ORDER_RELEASE,  // trả hàng về tồn khả dụng khi đơn hủy
    ADJUSTMENT,     // điều chỉnh (kế toán)
    RETURN,         // trả hàng
}
