package com.fashion.payment.properties.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fashion.payment.properties.BaseCacheProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Configuration
@Data
@ConfigurationProperties(prefix = "app.cache.payment")
@EqualsAndHashCode(callSuper = true)
public class PaymentServiceCacheProperties extends BaseCacheProperties<PaymentServiceCacheProperties.Keys>{
    @Data
    public static class Keys {
        private String paymentInfo;
        private String paymentMethodInfo;
    }
}
