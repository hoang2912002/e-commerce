package com.fashion.resource.common.enums;

import lombok.Getter;

@Getter
public enum FileEnum {
    USER_AVATAR("user"), 
    PRODUCT_THUMBNAIL("product"),
    PRODUCT_SKU_THUMBNAIL("product-sku"),
    SM_BUSINESS_LICENSE("shop-management"),
    SM_IDENTIFICATION_IMAGE_FIRST("shop-management"),
    SM_IDENTIFICATION_IMAGE_SECOND("shop-management"),
    SM_LOGO("shop-management"),
    SM_THUMBNAIL("shop-management");

    // folderPath dùng để lấy giá trị "user", "product",...
    private final String folderPath;

    FileEnum(String folderPath) {
        this.folderPath = folderPath;
    }
}
