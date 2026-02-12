package com.fashion.inventory.messaging.provider.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fashion.inventory.common.enums.EventType;
import com.fashion.inventory.dto.response.kafka.EventMetaData;
import com.fashion.inventory.dto.response.kafka.KafkaEvent;
import com.fashion.inventory.messaging.provider.InventoryServiceProvider;
import com.fashion.inventory.properties.KafkaTopicInventoryServiceProperties;
import com.fashion.inventory.service.KafkaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServiceProviderImpl implements InventoryServiceProvider{
    KafkaService kafkaService;
    KafkaTopicInventoryServiceProperties kafkaTopicInventoryServiceProperties;
    
    @Override
    public void produceInventoryCreatedSuccess(List<UUID> productSkuIds, EventMetaData metadata) {
        var topic = kafkaTopicInventoryServiceProperties.getInventoryCreatedSuccess();
        log.info("PRODUCT-SERVICE: produceInventoryCreatedSuccess(): inventory create successful to send event reset temp stock of product sku topic {}", topic);

        KafkaEvent<List<UUID>> message = KafkaEvent.<List<UUID>>builder()
            .metadata(EventMetaData.builder()
                .eventId(metadata.getEventId())
                .eventType(EventType.INVENTORY_CREATED_SUCCESS.name())
                .source("inventory-service")
                .version(1)
                .build())
            .payload(productSkuIds)
            .build();
        kafkaService.send(topic, message);
    }
    @Override
    public void produceInventoryCreationFailed(UUID productId) {
        var topic = kafkaTopicInventoryServiceProperties.getInventoryCreationFailed();
        log.info("PRODUCT-SERVICE: produceInventoryCreationFailed(): inventory create failed to send event create Product Approval with REJECT status {}", topic);

        KafkaEvent<UUID> message = KafkaEvent.<UUID>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.INVENTORY_CREATED_SUCCESS.name())
                .source("inventory-service")
                .version(1)
                .build())
            .payload(productId)
            .build();
        kafkaService.send(topic, message);
    }
    

}
