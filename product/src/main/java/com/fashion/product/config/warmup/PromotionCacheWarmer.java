package com.fashion.product.config.warmup;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fashion.product.service.PromotionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionCacheWarmer {
    PromotionService promotionService;
    @Scheduled(fixedDelay = 600000)
    public void refreshCache() {
        this.promotionService.warmPromotion();
    }
}
