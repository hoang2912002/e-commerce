package com.fashion.shipping.properties.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fashion.shipping.properties.BaseCacheProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Configuration
@Data
@ConfigurationProperties(prefix = "app.cache.shipping")
@EqualsAndHashCode(callSuper = true)
public class ShippingServiceGhnCacheProperties extends BaseCacheProperties<ShippingServiceGhnCacheProperties.Keys>{
    @Data
    public static class Keys {
        private String shippingInfo;
        private String shippingThirdPartyGhnWard;
        private String shippingThirdPartyGhnProvince;
        private String shippingThirdPartyGhnDistrict;
        private String shippingThirdPartyGhnFeeLeadTime;
    }
}
