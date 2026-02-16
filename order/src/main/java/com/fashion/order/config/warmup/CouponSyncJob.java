package com.fashion.order.config.warmup;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fashion.order.properties.cache.OrderServiceCacheProperties;
import com.fashion.order.repository.CouponRepository;
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
public class CouponSyncJob {
    StringRedisTemplate stringRedisTemplate;
    CouponRepository couponRepository;
    ObjectMapper objectMapper;
    OrderServiceCacheProperties orderServiceCacheProperties;

    @Scheduled(fixedDelay = 120000) // 2 minus
    public void syncCouponsToDatabase() {
        String pattern = orderServiceCacheProperties.getPrefix() + ":" + 
                         orderServiceCacheProperties.getKeys().getCouponInfo() + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        int synced = 0;
        int failed = 0;

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                
                // Loại bỏ lock key (thực tế pattern trên đã loại bỏ lock: rồi nhưng check lại cho chắc)
                if (key.startsWith("lock:")) continue;

                try {
                    syncSingleCouponByKey(key);
                    synced++;
                } catch (Exception e) {
                    log.error("COUPON-SYNC: Error syncing key {}: {}", key, e.getMessage());
                    failed++;
                }
            }
        } catch (Exception e) {
            log.error("COUPON-SYNC: Scan failed: {}", e.getMessage());
        }
    }

    private void syncSingleCouponByKey(String key) throws Exception {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) return;

        // Key: fsh:ord:couI:d9b07619... -> Index 3 là UUID
        String[] parts = key.split(":");
        if (parts.length < 4) return;
        
        UUID couponId = UUID.fromString(parts[3]);

        // Parse JSON (Lưu ý: Nếu ông dùng Jackson Default Typing nó sẽ có dạng Array ["class", {data}])
        JsonNode root = objectMapper.readTree(value);
        JsonNode dataNode = root.isArray() ? root.get(1) : root; // Linh hoạt cho cả 2 kiểu
        
        if (dataNode != null && dataNode.has("stock")) {
            Integer stock = dataNode.get("stock").asInt();

            // Ghi đè số lượng vào DB (Sử dụng lệnh update tuyệt đối vì Redis là nguồn chân lý)
            int updated = couponRepository.updateStockAtomic(couponId, stock);
            
            if (updated > 0) {
                log.debug("COUPON-SYNC: Updated DB for coupon {} -> stock: {}", couponId, stock);
            }
        }
    }
}
