package com.fashion.shipping.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public enum ShippingOperationEnum {
    CREATE(true, true, false),
    UPDATE(true, true, true),
    SAGA_CREATE(false, false, false),
    SAGA_UPDATE(false, false, true);

    private final boolean needsOrderValidation;
    private final boolean needsShippingFeeCalculation;
    private final boolean isUpdate;

    ShippingOperationEnum(boolean needsOrderValidation, boolean needsShippingFeeCalculation, boolean isUpdate) {
        this.needsOrderValidation = needsOrderValidation;
        this.needsShippingFeeCalculation = needsShippingFeeCalculation;
        this.isUpdate = isUpdate;
    }

    public boolean needsOrderValidation() {
        return needsOrderValidation;
    }

    public boolean needsShippingFeeCalculation() {
        return needsShippingFeeCalculation;
    }

    public boolean isUpdate() {
        return isUpdate;
    }
}
