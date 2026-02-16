package com.fashion.product.config.warmup;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fashion.product.properties.cache.ProductServiceCacheProperties;
import com.fashion.product.repository.PromotionRepository;
import com.fashion.product.service.PromotionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionSyncJob {
    StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper;
    PromotionService promotionService;
    ProductServiceCacheProperties productServiceCacheProperties;

    @Scheduled(fixedDelay = 120000)
    public void syncPromotionsToDatabase() {
        String pattern = productServiceCacheProperties.getPrefix() + ":" + 
                         productServiceCacheProperties.getKeys().getPromotionInfo() + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        int synced = 0;
        int failed = 0;

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                
                if (key.startsWith("lock:")) continue;

                try {
                    this.syncSinglePromotionByKey(key);
                    synced++;
                } catch (Exception e) {
                    log.error("PROMOTION-SYNC: Error syncing key {}: {}", key, e.getMessage());
                    failed++;
                }
            }
        } catch (Exception e) {
            log.error("PROMOTION-SYNC: Scan failed: {}", e.getMessage());
        }
    }

    private void syncSinglePromotionByKey(String key) throws Exception {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) return;

        String[] parts = key.split(":");
        if (parts.length < 4) return;
        
        UUID promotionId = UUID.fromString(parts[3]);

        // Parse JSON
        JsonNode root = objectMapper.readTree(value);
        JsonNode dataNode = root.isArray() ? root.get(1) : root; // Linh hoạt cho cả 2 kiểu
        
        if (dataNode != null && dataNode.has("quantity")) {
            Integer quantity = dataNode.get("quantity").asInt();

            this.promotionService.updateQuantityPromotion(promotionId, quantity);
        }
    }
}
