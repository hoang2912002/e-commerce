package com.fashion.order.messaging.provider.impls;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fashion.order.common.enums.EventType;
import com.fashion.order.dto.response.SagaStateResponse;
import com.fashion.order.dto.response.internal.InventoryResponse.ReturnAvailableQuantity;
import com.fashion.order.dto.response.internal.PaymentResponse.InnerInternalPayment;
import com.fashion.order.dto.response.internal.ShippingResponse;
import com.fashion.order.dto.response.kafka.EventMetaData;
import com.fashion.order.dto.response.kafka.KafkaEvent;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent.InternalOrderCreatedEvent;
import com.fashion.order.entity.SagaState;
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
    @Async("virtualExecutor")
    public CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessPayment(SagaState sagaState,InnerInternalPayment payment) {
        String topic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessPayment();
        String replyTopic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessReply();
        log.info("ORDER-SERVICE: [produceOrderCreatedEventSuccessPayment] Sending payment command to topic {}", topic);
        
        KafkaEvent<InnerInternalPayment> message = this.buildMetaData(EventType.SAGA_PAYMENT_COMMAND, payment);
        
        return kafkaService.sendAndWaitReply(
            topic,
            replyTopic,
            sagaState.getOrderId().toString(),
            message,
            SagaStateResponse.class,
            30000L // 30 seconds timeout
        );
    }

    @Override
    @Async("virtualExecutor")
    public CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessPromotion(SagaState sagaState,
            Map<UUID, Integer> promotions) {
        String topic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessPromotion();
        String replyTopic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessReply();
        log.info("ORDER-SERVICE: [produceOrderCreatedEventSuccessPayment] Sending promotion command to topic {}", topic);

        KafkaEvent<Map<UUID, Integer>> message = this.buildMetaData(EventType.SAGA_PROMOTION_COMMAND, promotions);
        
        return kafkaService.sendAndWaitReply(
            topic,
            replyTopic,
            sagaState.getOrderId().toString(),
            message,
            SagaStateResponse.class,
            30000L
        );

    }

    @Override
    @Async("virtualExecutor")
    public CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessInventory(SagaState sagaState,
            Collection<ReturnAvailableQuantity> inventories) {
        String topic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessInventory();
        String replyTopic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessReply();
        log.info("ORDER-SERVICE: [produceOrderCreatedEventSuccessPayment] Sending inventory command to topic {}", topic);

        KafkaEvent<Collection<ReturnAvailableQuantity>> message = this.buildMetaData(EventType.SAGA_INVENTORY_COMMAND, inventories);
        
        return kafkaService.sendAndWaitReply(
            topic,
            replyTopic,
            sagaState.getOrderId().toString(),
            message,
            SagaStateResponse.class,
            30000L
        );
    }

    @Override
    @Async("virtualExecutor")
    public CompletableFuture<Void> produceOrderCreatedEventSuccessPaymentFailed(SagaState sagaState,
            InnerInternalPayment event) {
        String topic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessPaymentFailed();
        log.warn("ORDER-SERVICE: [produceOrderCreatedEventSuccessPaymentFailed] Sending payment compensation to topic {}", topic);
        
        // KafkaEvent<UUID> message = this.buildMetaData(EventType.SAGA_PAYMENT_COMPENSATION, event.getOrderId());
        KafkaEvent<InnerInternalPayment> message = this.buildMetaData(EventType.SAGA_PAYMENT_COMMAND, event);
        kafkaService.send(topic, sagaState.getOrderId().toString(), message);
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> produceOrderCreatedEventSuccessPromotionFailed(SagaState sagaState,
            Map<UUID, Integer> promotions) {
        String topic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessPromotionFailed();
        log.warn("ORDER-SERVICE: [produceOrderCreatedEventSuccessPromotionFailed] Sending promotion compensation to topic {}", topic);

        KafkaEvent<Map<UUID, Integer>> message = this.buildMetaData(EventType.SAGA_PROMOTION_COMPENSATION, promotions);
        kafkaService.send(topic, sagaState.getOrderId().toString(), message);
        
        return CompletableFuture.completedFuture(null);       
    }

    @Override
    public CompletableFuture<Void> produceOrderCreatedEventSuccessInventoryFailed(SagaState sagaState,
            Collection<ReturnAvailableQuantity> inventories) {
        String topic = this.kafkaTopicOrderProperties.getOrderCreatedSuccessInventoryFailed();
        log.warn("ORDER-SERVICE: [produceOrderCreatedEventSuccessInventoryFailed] Sending inventory compensation to topic {}", topic);

        KafkaEvent<Collection<ReturnAvailableQuantity>> message = this.buildMetaData(EventType.SAGA_PROMOTION_COMPENSATION, inventories);
        kafkaService.send(topic, sagaState.getOrderId().toString(), message);
        
        return CompletableFuture.completedFuture(null);       
    }

    @Override
    public CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessShipping(SagaState sagaState,
            ShippingResponse shipping) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'produceOrderCreatedEventSuccessShipping'");
    }

    @Override
    public CompletableFuture<Void> produceOrderCreatedEventSuccessShippingFailed(SagaState sagaState,
            ShippingResponse shipping) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'produceOrderCreatedEventSuccessShippingFailed'");
    }

    private <T> KafkaEvent<T> buildMetaData(EventType eventType, T payload){
        return KafkaEvent.<T>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType.name())
                .source("order-service")
                .version(1)
                .build())
            .payload(payload)
            .build();
    }
}
