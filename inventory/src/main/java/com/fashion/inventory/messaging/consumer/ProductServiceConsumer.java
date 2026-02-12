package com.fashion.inventory.messaging.consumer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.inventory.common.util.AsyncUtils;
import com.fashion.inventory.dto.response.kafka.EventMetaData;
import com.fashion.inventory.dto.response.kafka.KafkaEvent;
import com.fashion.inventory.dto.response.kafka.ProductApprovedEvent;
import com.fashion.inventory.messaging.provider.InventoryServiceProvider;
import com.fashion.inventory.service.InventoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceConsumer {
    InventoryService inventoryService;
    Executor virtualExecutor;
    InventoryServiceProvider inventoryServiceProvider;
    
    @KafkaListener(
        topics = "${spring.kafka.topic.product.approval-history-approved-product-success}", 
        groupId = "${spring.kafka.group.product}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    public void onMessageHandlerUpSertInventory(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment ack
    ){
        KafkaEvent<List<ProductApprovedEvent>> event = null;
        long startTime = System.currentTimeMillis();
        try {
            log.info("[onMessageHandlerUpSertInventory] Start consuming message ...");
            log.info("[onMessageHandlerUpSertInventory] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();
            event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<List<ProductApprovedEvent>>>() {
            });

            EventMetaData metadata = event.getMetadata();
            List<ProductApprovedEvent> inventoriesData = event.getPayload();

            if (inventoriesData.isEmpty()) {
                log.warn("INVENTORY-SERVICE: Empty payload from Approval history: {}", metadata.getSource());
                ack.acknowledge();
                return;
            }

            CompletableFuture<List<UUID>> inventoryFuture = 
                AsyncUtils.fetchAsyncWThread(
                    () -> this.inventoryService.adjustmentsStockAfterProductApproved(
                        inventoriesData, 
                        metadata.getEventId()
                    ),
                    virtualExecutor
                );
            
            List<UUID> productSkuIds = inventoryFuture.join();
            
            CompletableFuture<Void> publishFuture = CompletableFuture.runAsync(
                () -> this.inventoryServiceProvider.produceInventoryCreatedSuccess(productSkuIds, metadata),
                virtualExecutor
            );

            publishFuture.join();
            ack.acknowledge();

            long duration = System.currentTimeMillis() - startTime;
            log.info("INVENTORY: Successfully processed message from partition {} in {}ms", 
                partition, duration);
        } catch (Exception e) {
            log.error("INVENTORY: Error processing message: {}", e.getMessage(), e);
            
            if (event != null && event.getPayload() != null) {
                UUID productId = event.getPayload().stream()
                    .map(ProductApprovedEvent::getProductId)
                    .findFirst().get();
                
                this.inventoryServiceProvider.produceInventoryCreationFailed(productId);
                ack.acknowledge();
            }
        }
    }
}
