package com.fashion.inventory.service.provider;

import org.springframework.stereotype.Component;

import com.fashion.inventory.common.enums.EnumError;
import com.fashion.inventory.common.enums.WareHouseStatusEnum;
import com.fashion.inventory.common.provider.WareHouseErrorProvider;

@Component
public class WareHouseUpSertOrderErrorProvider implements WareHouseErrorProvider {
    @Override
    public EnumError getError(WareHouseStatusEnum status) {
        return switch(status) {
            case PENDING -> EnumError.INVENTORY_DATA_STATUS_PENDING_STATUS_CHANGE_MUST_BE_ACTIVE_CLOSED;
            case ACTIVE -> EnumError.INVENTORY_DATA_STATUS_ACTIVE_STATUS_CHANGE_MUST_BE_INACTIVE_CLOSED;
            case INACTIVE -> EnumError.INVENTORY_DATA_STATUS_INACTIVE_STATUS_CHANGE_MUST_BE_ACTIVE_CLOSED;
            case CLOSED -> EnumError.INVENTORY_DATA_STATUS_CLOSED_STATUS_CHANGE_NOT_ALLOWED;
            default -> EnumError.INVENTORY_INTERNAL_ERROR_CALL_API;
        };
    }

    @Override
    public String getMessageCode(WareHouseStatusEnum status) {
        return switch(status){
            case PENDING -> "ware.house.current.status.pending.cannot.create.update";
            case ACTIVE -> "ware.house.current.status.active.cannot.create.update";
            case INACTIVE -> "ware.house.current.status.inactive.cannot.create.update";
            case CLOSED -> "ware.house.current.status.closed.cannot.create.update";
            default -> "server.error.internal";
        };
    }
}