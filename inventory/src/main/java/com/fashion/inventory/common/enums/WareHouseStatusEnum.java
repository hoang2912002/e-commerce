package com.fashion.inventory.common.enums;

import java.util.List;
import java.util.Map;

import com.fashion.inventory.common.provider.WareHouseErrorProvider;
import com.fashion.inventory.exception.ServiceException;

public enum WareHouseStatusEnum {
    PENDING, 
    ACTIVE,
    INACTIVE, // Kho tạm dừng hoạt động (ví dụ: sửa chữa, kiểm kê).
    CLOSED,
    ;

    public void validateUpSertAbility(WareHouseErrorProvider errorProvider, Map<String, Object> params){
        boolean isValid = switch (this) {
            case PENDING -> true;
            case ACTIVE, INACTIVE, CLOSED -> false;
        };

        if (!isValid) {
            throw new ServiceException(
                errorProvider.getError(this),
                errorProvider.getMessageCode(this),
                params
            );
        }
    }

    public void validateUpdateStatusAbility(WareHouseStatusEnum wareHouseStatusEnum, WareHouseErrorProvider errorProvider, Map<String, Object> params){
        boolean isValid = switch (this) {
            case PENDING -> List.of(WareHouseStatusEnum.ACTIVE, WareHouseStatusEnum.CLOSED).contains(wareHouseStatusEnum);
            case INACTIVE -> List.of(WareHouseStatusEnum.ACTIVE, WareHouseStatusEnum.CLOSED).contains(wareHouseStatusEnum);
            case ACTIVE -> List.of(WareHouseStatusEnum.INACTIVE, WareHouseStatusEnum.CLOSED).contains(wareHouseStatusEnum);
            case CLOSED -> false;
        };

        if (!isValid) {
            throw new ServiceException(
                errorProvider.getError(this),
                errorProvider.getMessageCode(this),
                params
            );
        }
    }

    // Check warehouse must be ACTIVE for CREATE/UPDATE Order
    public void validateOrderAbility(WareHouseErrorProvider errorProvider, Map<String, Object> params){
        boolean isValid = switch (this) {
            case ACTIVE -> true;
            case PENDING, INACTIVE, CLOSED -> false;
        };

        if (!isValid) {
            throw new ServiceException(
                errorProvider.getError(this),
                errorProvider.getMessageCode(this),
                params
            );
        }
    }
}
