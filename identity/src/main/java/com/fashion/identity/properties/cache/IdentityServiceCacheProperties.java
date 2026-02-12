package com.fashion.identity.properties.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fashion.identity.properties.BaseCacheProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
@Configuration
@Data
@ConfigurationProperties(prefix = "app.cache.identity")
@EqualsAndHashCode(callSuper = true)
public class IdentityServiceCacheProperties extends BaseCacheProperties<IdentityServiceCacheProperties.Keys>{
    @Data
    public static class Keys {
        private String userInfo;
        private String roleInfo;
        private String permissionInfo;
    }
}
