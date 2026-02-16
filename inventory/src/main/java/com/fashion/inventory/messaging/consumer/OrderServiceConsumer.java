package com.fashion.inventory.messaging.consumer;

import java.util.Collection;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.fashion.inventory.dto.request.InventoryRequest.ReturnAvailableQuantity;
import com.fashion.inventory.dto.response.kafka.EventMetaData;
import com.fashion.inventory.dto.response.kafka.KafkaEvent;
import com.fashion.inventory.dto.response.kafka.OrderCreatedEvent;
import com.fashion.inventory.dto.response.kafka.SagaStateResponse;
import com.fashion.inventory.entity.Inventory;
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
public class OrderServiceConsumer {
    InventoryService inventoryService;
    ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-inventory}", 
        groupId = "${spring.kafka.group.inventory}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    @SendTo
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessInventory(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo,
        Acknowledgment ack
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessInventory] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessInventory] Received message payload: {}", message);

            KafkaEvent<Collection<ReturnAvailableQuantity>> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<Collection<ReturnAvailableQuantity>>>() {
            });

            EventMetaData metadata = event.getMetadata();
            Collection<ReturnAvailableQuantity> orderData = event.getPayload();

            if(orderData.size() <= 0){
                log.info("INVENTORY-SERVICE: [onMessageHandlerOrderUpSertSuccessInventory] Inventories data is null");
                ack.acknowledge();
                return SagaStateResponse.failure("Inventories data is null");
            }
            ack.acknowledge();
            this.inventoryService.changeQuantityUse(orderData, metadata.getEventId());
            return SagaStateResponse.success();
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: onMessageHandlerOrderUpSertSuccessInventory: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure("Inventories processing failed: " + e.getMessage());
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-inventory-failed}", 
        groupId = "${spring.kafka.group.inventory}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    @SendTo
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessInventoryFailed(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo,
        Acknowledgment ack
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessInventoryFailed] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessInventoryFailed] Received message payload: {}", message);

            KafkaEvent<Collection<ReturnAvailableQuantity>> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<Collection<ReturnAvailableQuantity>>>() {
            });

            EventMetaData metadata = event.getMetadata();
            Collection<ReturnAvailableQuantity> orderData = event.getPayload();

            if(orderData.size() > 0){
                ack.acknowledge();
                return SagaStateResponse.success();
                // this.inventoryService.changeQuantityUse(orderData, metadata.getEventId());
            } else {
                log.info("INVENTORY-SERVICE: [onMessageHandlerOrderUpSertSuccessInventoryFailed] Inventories data is null");
                ack.acknowledge();
                return SagaStateResponse.failure("Inventories data is null");
            }
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: onMessageHandlerOrderUpSertSuccessInventoryFailed: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure("Inventories processing failed: " + e.getMessage());
        }
    }
}
