package com.fashion.inventory.config.warmup;

import java.util.UUID;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fashion.inventory.common.util.ConvertUuidUtil;
import com.fashion.inventory.properties.cache.InventoryServiceCacheProperties;
import com.fashion.inventory.service.InventoryService;
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
public class InventorySyncJob {
    StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper;
    InventoryServiceCacheProperties inventoryServiceCacheProperties;
    InventoryService inventoryService;

    @Scheduled(fixedDelay = 120000)
    public void syncInventoriesToDatabase() {
        String pattern = inventoryServiceCacheProperties.getPrefix() + ":" + 
                         inventoryServiceCacheProperties.getKeys().getInventoryInfoOrder() + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        int synced = 0;
        int failed = 0;

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                
                if (key.startsWith("lock:")) continue;

                try {
                    syncSingleInventoryByKey(key);
                    synced++;
                } catch (Exception e) {
                    log.error("INVENTORY-SYNC: Error syncing key {}: {}", key, e.getMessage());
                    failed++;
                }
            }
        } catch (Exception e) {
            log.error("INVENTORY-SYNC: Scan failed: {}", e.getMessage());
        }
    }

    private void syncSingleInventoryByKey(String key) throws Exception {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) return;

        // Key: fsh:ord:couI:d9b07619... -> Index 3 là UUID
        String[] parts = key.split(":");
        if (parts.length < 4) return;
        
        UUID productSkuId = UUID.fromString(parts[3]);
        UUID wareHouseId = UUID.randomUUID();
        // Parse JSON (Lưu ý: Nếu ông dùng Jackson Default Typing nó sẽ có dạng Array ["class", {data}])
        JsonNode root = objectMapper.readTree(value);
        JsonNode dataNode = root.isArray() ? root.get(1) : root; // Linh hoạt cho cả 2 kiểu

        JsonNode wareHouseArray = dataNode.get("wareHouse");
        if (wareHouseArray != null && wareHouseArray.isArray() && wareHouseArray.size() >= 2) {
            JsonNode wareHouseData = wareHouseArray.get(1);
            
            String idStr = wareHouseData.get("id").asText();
            
            wareHouseId = ConvertUuidUtil.toUuid(idStr);
        }
        if (dataNode != null && dataNode.has("quantityAvailable") && dataNode.has("quantityReserved")) {
            Integer quantityAvailable = dataNode.get("quantityAvailable").asInt();
            Integer quantityReserved = dataNode.get("quantityReserved").asInt();
            UUID productId = ConvertUuidUtil.toUuid(dataNode.get("productId").asText("null"));

            this.inventoryService.updateQuantityInventory(productSkuId, productId,quantityAvailable, quantityReserved, wareHouseId);
        }
    }
}
