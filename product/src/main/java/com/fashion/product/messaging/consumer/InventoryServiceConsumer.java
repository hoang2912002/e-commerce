package com.fashion.product.messaging.consumer;

import java.util.List;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.dto.response.kafka.KafkaEvent;
import com.fashion.product.dto.response.kafka.OrderCreatedEvent;
import com.fashion.product.exception.KafkaException;
import com.fashion.product.service.ApprovalHistoryService;
import com.fashion.product.service.ProductSkuService;
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
public class InventoryServiceConsumer {
    ProductSkuService productSkuService;
    ApprovalHistoryService approvalHistoryService;

    @KafkaListener(
        topics = "${spring.kafka.topic.inventory.inventory-created-success}", 
        groupId = "${spring.kafka.group.product}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    public void onMessageHandlerInventoryCreatedSuccess(
        @Payload String message, 
        Acknowledgment ack,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ){
        try {
            log.info("[onMessageHandlerInventoryCreatedSuccess] Start consuming message ...");
            log.info("[onMessageHandlerInventoryCreatedSuccess] Received message payload: {}", message);
            
            ObjectMapper objectMapper = new ObjectMapper();
            KafkaEvent<List<UUID>> event = objectMapper.readValue(message, 
                new TypeReference<KafkaEvent<List<UUID>>>() {}
            );

            List<UUID> productSkuIds = event.getPayload();

            this.productSkuService.completeSagaResetTempStock(productSkuIds);

            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [onInventoryCreatedSuccess] Error: {}", e.getMessage(), e);
            throw new KafkaException(EnumError.PRODUCT_KAFKA_SAGA_COMPLETED_MESSAGE_ERROR,"kafka.error.consume-completed-message-error");
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.inventory.inventory-creation-failed}",
        groupId = "${spring.kafka.group.product}",
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    public void onMessageHandlerInventoryCreationFailed(@Payload String message, Acknowledgment ack) {
        try {
            log.info("[onMessageHandlerInventoryCreationFailed] Start consuming message ...");
            log.info("[onMessageHandlerInventoryCreationFailed] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();
            KafkaEvent<UUID> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<UUID>>() {}
            );

            UUID productId = event.getPayload();
            
            // ✅ Compensate: Restore tempStock, rollback approval if needed
            this.approvalHistoryService.failedSagaRejectProduct(productId, event.getMetadata().getEventId());
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [onMessageHandlerInventoryCreationFailed] Error: {}", e.getMessage(), e);
            throw new KafkaException(EnumError.PRODUCT_KAFKA_SAGA_FAILED_MESSAGE_ERROR,"kafka.error.consume-failed-message-error");
        }
    }
}
