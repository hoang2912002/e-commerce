package com.fashion.order.common.provider;

import com.fashion.order.common.enums.ApprovalMasterEnum;
import com.fashion.order.common.enums.EnumError;
import com.fashion.order.common.enums.OrderEnum;

public interface OrderErrorProvider {
    EnumError getError(OrderEnum status);
    String getMessageCode(OrderEnum status);
}
