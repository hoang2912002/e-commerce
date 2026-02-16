package com.fashion.inventory.config.warmup;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fashion.inventory.common.util.AsyncUtils;
import com.fashion.inventory.dto.response.InventoryResponse;
import com.fashion.inventory.dto.response.internal.ProductResponse;
import com.fashion.inventory.entity.Inventory;
import com.fashion.inventory.mapper.InventoryMapper;
import com.fashion.inventory.repository.InventoryRepository;
import com.fashion.inventory.service.InventoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryCacheWarm {
    InventoryService inventoryService;
    Executor virtualExecutor;
    @Scheduled(fixedDelay = 600000)
    public void refreshCache() {
        CompletableFuture<Void> warmIFuture = AsyncUtils.fetchVoidWThread(() -> this.inventoryService.warmingInventory(), virtualExecutor);
        CompletableFuture<Void> warmIOFuture = AsyncUtils.fetchVoidWThread(() -> this.inventoryService.warmingInventoryOrder(), virtualExecutor);
        CompletableFuture.allOf(warmIFuture, warmIOFuture);
        warmIFuture.join();
        warmIOFuture.join();
    }
}
