package com.fashion.inventory.messaging.consumer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.inventory.dto.response.kafka.EventMetaData;
import com.fashion.inventory.dto.response.kafka.KafkaEvent;
import com.fashion.inventory.dto.response.kafka.ProductApprovedEvent;
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
    
    @KafkaListener(topics = "${spring.kafka.topic.product.approval-history-approved-product-success}", groupId = "${spring.kafka.group.product}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessageHandlerUpSertInventory(@Payload String message){
        try {
            log.info("[onMessageHandlerUpSertInventory] Start consuming message ...");
            log.info("[onMessageHandlerUpSertInventory] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();

            KafkaEvent<List<ProductApprovedEvent>> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<List<ProductApprovedEvent>>>() {
            });

            EventMetaData metadata = event.getMetadata();
            List<ProductApprovedEvent> inventoriesData = event.getPayload();

            if(!inventoriesData.isEmpty()){
                this.inventoryService.adjustmentsStockAfterProductApproved(inventoriesData,metadata.getEventId());
            } else {
                log.info("NOTIFICATION-SERVICE: [onMessageHandlerSendMailVerifyCode] Incorrect format user data receive message from kafka: {}", metadata.getSource());
            }
        } catch (Exception e) {
            log.error("NOTIFICATION-SERVICE: onMessageHandlerSendMailVerifyCode: {}", e.getMessage());
        }
    }
}
