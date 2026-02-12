package com.fashion.inventory.properties.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fashion.inventory.properties.BaseCacheProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Configuration
@Data
@ConfigurationProperties(prefix = "app.cache.product")
@EqualsAndHashCode(callSuper = true)
public class ProductServiceCacheProperties extends BaseCacheProperties<ProductServiceCacheProperties>{
    @Data
    public static class Keys {
        private String productInfo;
        private String shopManagementInfo;
        private String promotionInfo;
        private String approvalHistoryInfo;
        private String categoryInfo;
        private String shopManagementEntityInfo;
    }
}
