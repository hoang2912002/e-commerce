package com.fashion.order.service.provider;

import org.springframework.stereotype.Component;

import com.fashion.order.common.enums.EnumError;
import com.fashion.order.common.enums.OrderEnum;
import com.fashion.order.common.provider.OrderErrorProvider;
@Component
public class OrderUpdateStatusErrorProvider implements OrderErrorProvider{
    @Override
    public EnumError getError(OrderEnum status) {
        return switch(status) {
            case PENDING -> EnumError.ORDER_ORDER_STATUS_PENDING_CANNOT_UPDATE;
            case CONFIRMED -> EnumError.ORDER_ORDER_STATUS_CONFIRMED_CANNOT_UPDATE;
            case SHIPPING -> EnumError.ORDER_ORDER_STATUS_SHIPPING_CANNOT_UPDATE;
            case DELIVERED -> EnumError.ORDER_ORDER_STATUS_DELIVERED_CANNOT_UPDATE;
            case CANCELLED -> EnumError.ORDER_ORDER_STATUS_CANCELLED_CANNOT_UPDATE;
            case RETURNED -> EnumError.ORDER_ORDER_STATUS_RETURNED_CANNOT_UPDATE;
        };
    }

    @Override
    public String getMessageCode(OrderEnum status) {
       return switch(status) {
            case PENDING -> "order.status.pending.cannot.update";
            case CONFIRMED -> "order.status.confirmed.cannot.update";
            case SHIPPING -> "order.status.shipping.cannot.update";
            case DELIVERED -> "order.status.delivered.cannot.update";
            case CANCELLED -> "order.status.canceled.cannot.update";
            case RETURNED -> "order.status.returned.cannot.updater";
        }; 
    }
    
}
