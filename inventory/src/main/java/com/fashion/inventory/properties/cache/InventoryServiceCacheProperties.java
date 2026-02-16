package com.fashion.inventory.properties.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fashion.inventory.properties.BaseCacheProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Configuration
@Data
@ConfigurationProperties(prefix = "app.cache.inventory")
@EqualsAndHashCode(callSuper = true)
public class InventoryServiceCacheProperties extends BaseCacheProperties<InventoryServiceCacheProperties.Keys>{
    @Data
    public static class Keys {
        private String inventoryInfo;
        private String wareHouseInfo;
        private String inventoryTransactionInfo;
        private String inventoryInfoOrder;
    }
}
