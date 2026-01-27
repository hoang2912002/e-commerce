package com.fashion.inventory.messaging.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.inventory.dto.response.kafka.EventMetaData;
import com.fashion.inventory.dto.response.kafka.KafkaEvent;
import com.fashion.inventory.dto.response.kafka.OrderCreatedEvent;
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

    @KafkaListener(topics = "${spring.kafka.topic.order.order-created-success}", groupId = "${spring.kafka.group.inventory}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessageHandlerOrderUpSertSuccess(@Payload String message){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccess] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccess] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();

            KafkaEvent<OrderCreatedEvent> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<OrderCreatedEvent>>() {
            });

            EventMetaData metadata = event.getMetadata();
            OrderCreatedEvent orderData = event.getPayload();

            if(orderData.getInventories().size() > 0){
                this.inventoryService.changeQuantityUse(orderData.getInventories(), metadata.getEventId());
            } else {
                log.info("INVENTORY-SERVICE: [onMessageHandlerOrderUpSertSuccess] Not found inventories data received message from kafka with sender ORDER-SERVICE: {}", metadata.getSource());
            }
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: onMessageHandlerOrderUpSertSuccess: {}", e.getMessage());
        }
    }
}
