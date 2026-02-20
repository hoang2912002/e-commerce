package com.fashion.shipping.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
@ConfigurationProperties(prefix = "shipping.ghn")
public class ShippingGhnProperties {
    String apiTokenDev;
    String apiTokenProduction;
    String baseUrlDev;
    String baseUrlProduction;
    Long shopIdDev;
    Long shopIdProduction;
    String shopAddressDistrictName;
    Long shopAddressDistrictId;
    String shopAddressWardName;
    String shopAddressWardId;
    String shopAddressProvinceName;
    Long shopAddressProvinceId;
    String shopAddressHouseNumberName;
}
