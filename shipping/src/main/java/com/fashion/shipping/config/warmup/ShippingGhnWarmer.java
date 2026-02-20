package com.fashion.shipping.config.warmup;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fashion.shipping.service.strategy.ShippingStrategy;
import com.fashion.shipping.service.strategy.impl.ShippingGhnStrategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShippingGhnWarmer {
    ShippingStrategy shippingStrategy;

    @Scheduled(cron = "0 0 2 * * *")
    public void warmAddressCache() {
        this.shippingStrategy.warmAddressCache();
    }    
}
