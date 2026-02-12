package com.fashion.product.messaging.provider.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fashion.product.common.enums.EventType;
import com.fashion.product.dto.response.kafka.EventMetaData;
import com.fashion.product.dto.response.kafka.KafkaEvent;
import com.fashion.product.dto.response.kafka.ProductApprovedEvent;
import com.fashion.product.dto.response.kafka.ShopManagementAddressEvent;
import com.fashion.product.dto.response.kafka.ProductApprovedEvent.InternalProductApprovedEvent;
import com.fashion.product.dto.response.kafka.ShopManagementAddressEvent.InternalShopManagementAddressEvent;
import com.fashion.product.messaging.provider.ProductServiceProvider;
import com.fashion.product.properties.KafkaTopicApprovalHistoryProperties;
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
    KafkaTopicApprovalHistoryProperties kafkaTopicApprovalHistoryProperties;

    @Override
    @Async("virtualExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void produceShopManagementEventSuccess(InternalShopManagementAddressEvent event) {
        var topic = kafkaTopicShopManagementProperties.getShopManagementCreatedSuccess();
        log.info("PRODUCT-SERVICE: produceShopManagementEventSuccess(): shop management create successful to send event create address topic {}", topic);

        KafkaEvent<ShopManagementAddressEvent> message = KafkaEvent.<ShopManagementAddressEvent>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.SHOP_MANAGEMENT_CREATED.name())
                .source("product-service")
                .version(1)
                .build())
            .payload(event.getShopManagementAddressEvent())
            .build();
        kafkaService.send(topic, message);
    }

    @Override
    @Async("virtualExecutor") // Chạy thread riêng để không block luồng chính
    // phase = AFTER_COMMIT đảm bảo DB xong xuôi mới gửi tin nhắn
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void produceProductApprovedEventSuccess(InternalProductApprovedEvent event) {
        var topic = kafkaTopicApprovalHistoryProperties.getApprovalHistoryApprovedProductSuccess();
        log.info("PRODUCT-SERVICE: produceProductApprovedEventSuccess(): approval history approved successful for product to send event create inventory topic {}", topic);
        
        KafkaEvent<List<ProductApprovedEvent>> message = KafkaEvent.<List<ProductApprovedEvent>>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.APPROVAL_HISTORY_APPROVED.name())
                .source("product-service")
                .version(1)
                // .correlationId(UUID.randomUUID().toString())
                .build())
            .payload(event.getInventoriesData())
            .build();
        kafkaService.send(topic, message);
    }
    
}
