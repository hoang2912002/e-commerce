package com.fashion.inventory.common.enums;

import java.util.Set;

public enum OrderEnum {
    PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, RETURNED;

    // Luồng trạng thái đơn hàng 
    // Method instance (non-static method)
    public Set<OrderEnum> getValidNextStatuses() {
        return switch (this) {
            case PENDING -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(SHIPPING, CANCELLED);
            case SHIPPING -> Set.of(DELIVERED, RETURNED);
            case DELIVERED -> Set.of(RETURNED); // Sau khi giao hàng thành công, chỉ có thể trả hàng
            case CANCELLED, RETURNED -> Set.of(); // Trạng thái cuối cùng, không thể chuyển đổi tiếp
        };
    }
}
