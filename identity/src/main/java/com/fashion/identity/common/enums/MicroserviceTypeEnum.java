package com.fashion.identity.common.enums;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public enum MicroserviceTypeEnum {
    IDENTITY_SERVICE("identity-service"),
    PRODUCT_SERVICE("product-service"),
    ORDER_SERVICE("order-service"),
    INVENTORY_SERVICE("inventory-service"),
    RESOURCE_SERVICE("resource-service"),
    PAYMENT_SERVICE("payment-service"),
    SHIPPING_SERVICE("shipping-service"),
    NOTIFICATION_SERVICE("notification-service"),
    ;

    String value;

    MicroserviceTypeEnum(String value) {
        this.value = value;
    }

    public static MicroserviceTypeEnum fromValue(String value) {
        return Arrays.stream(MicroserviceTypeEnum.values())
                .filter(service -> service.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Microservice name: " + value));
    }
}
