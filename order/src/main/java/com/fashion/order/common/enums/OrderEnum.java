package com.fashion.order.common.enums;

import java.util.Map;
import java.util.Set;

import com.fashion.order.common.provider.OrderErrorProvider;
import com.fashion.order.exception.ServiceException;

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

    public void validateUpdateOrder(OrderErrorProvider errorProvider){
        boolean invalid = switch (this) {
            case PENDING, CONFIRMED -> true;
            case SHIPPING, DELIVERED, CANCELLED, RETURNED -> false;
        };

        if(!invalid){
            throw new ServiceException(
                errorProvider.getError(this),
                errorProvider.getMessageCode(this)
            );
        }
    }
}
