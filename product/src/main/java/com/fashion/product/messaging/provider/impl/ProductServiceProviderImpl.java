package com.fashion.product.messaging.provider.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fashion.product.common.enums.EventType;
import com.fashion.product.dto.response.kafka.EventMetaData;
import com.fashion.product.dto.response.kafka.KafkaEvent;
import com.fashion.product.dto.response.kafka.ShopManagementAddressEvent;
import com.fashion.product.messaging.provider.ProductServiceProvider;
import com.fashion.product.properties.KafkaTopicShopManagementProperties;
import com.fashion.product.service.KafkaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceProviderImpl implements ProductServiceProvider{
    KafkaService kafkaService;
    KafkaTopicShopManagementProperties kafkaTopicShopManagementProperties;

    @Override
    public void produceShopManagementEventSuccess(ShopManagementAddressEvent addressData) {
        var topic = kafkaTopicShopManagementProperties.getShopManagementCreatedSuccess();
        log.info("PRODUCT-SERVICE: produceShopManagementEventSuccess(): shop management create successful to send event create address topic {}", topic);

        KafkaEvent<ShopManagementAddressEvent> message = KafkaEvent.<ShopManagementAddressEvent>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.SHOP_MANAGEMENT_CREATED.name())
                .source("product-service")
                .version(1)
                .build())
            .payload(addressData)
            .build();
        kafkaService.send(topic, message);
    }
    
}
