package com.fashion.inventory.common.enums;

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
}
