package com.fashion.order.messaging.provider.impls;

import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fashion.order.common.enums.EventType;
import com.fashion.order.dto.response.kafka.EventMetaData;
import com.fashion.order.dto.response.kafka.KafkaEvent;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent.InternalOrderCreatedEvent;
import com.fashion.order.messaging.provider.OrderServiceProvider;
import com.fashion.order.properties.KafkaTopicOrderProperties;
import com.fashion.order.service.KafkaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceProviderImpl implements OrderServiceProvider{
    KafkaTopicOrderProperties kafkaTopicOrderProperties;
    KafkaService kafkaService;
    @Override
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void produceOrderCreatedEventSuccess(InternalOrderCreatedEvent event) {
        String topic = kafkaTopicOrderProperties.getOrderCreatedSuccess();
        log.info("ORDER-SERVICE: produceOrderCreatedEventSuccess(): order created successful to send event turning quantity inventory, promotion and re-write shipping/payment data topic {}", topic);
        
        KafkaEvent<OrderCreatedEvent> message = KafkaEvent.<OrderCreatedEvent>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.ORDER_CREATED.name())
                .source("order-service")
                .version(1)
                .build())
            .payload(event.getOrderData())
            .build();
        kafkaService.send(topic, message);
    }
    
}
