package com.fashion.order.properties.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fashion.order.properties.BaseCacheProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
@Configuration
@Data
@ConfigurationProperties(prefix = "app.cache.order")
@EqualsAndHashCode(callSuper = true)
public class OrderServiceCacheProperties extends BaseCacheProperties<OrderServiceCacheProperties.Keys>{
    @Data
    public static class Keys {
        private String orderInfo;
        private String couponInfo;
    }
}
