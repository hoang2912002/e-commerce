package com.fashion.inventory.common.provider;

import com.fashion.inventory.common.enums.EnumError;
import com.fashion.inventory.common.enums.WareHouseStatusEnum;

public interface WareHouseErrorProvider {
    EnumError getError(WareHouseStatusEnum status);
    String getMessageCode(WareHouseStatusEnum status);
}
