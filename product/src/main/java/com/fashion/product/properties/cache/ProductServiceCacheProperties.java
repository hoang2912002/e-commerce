package com.fashion.product.properties.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;

@Configuration
@Data
@ConfigurationProperties(prefix = "app.cache.product")
public class ProductServiceCacheProperties {
    private String prefix;
    private Keys keys;
    private String lock;

    @Data
    public static class Keys {
        private String productInfo;
        private String shopManagementInfo;
        private String promotionInfo;
        private String approvalHistoryInfo;
        private String categoryInfo;
        private String shopManagementEntityInfo;
    }

    public String createCacheKey(String keyPath, Object identifier) {
        return String.format("%s:%s:%s", prefix, keyPath, identifier.toString());
    }

    public String createLockKey(String keyPath, Object identifier) {
        return String.format("%s:%s:%s", lock, keyPath, identifier.toString());
    }
}
