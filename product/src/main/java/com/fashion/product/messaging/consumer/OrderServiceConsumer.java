package com.fashion.product.messaging.consumer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.fashion.product.dto.response.kafka.EventMetaData;
import com.fashion.product.dto.response.kafka.KafkaEvent;
import com.fashion.product.dto.response.kafka.OrderCreatedEvent;
import com.fashion.product.dto.response.kafka.SagaStateResponse;
import com.fashion.product.service.PromotionService;
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
    PromotionService promotionService;
    ObjectMapper objectMapper;
    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-promotion}", 
        groupId = "${spring.kafka.group.product}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    @SendTo
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessPromotion(
        @Payload String message,
        Acknowledgment ack,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessPromotion] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessPromotion] Received message payload: {}", message);

            KafkaEvent<Map<UUID, Integer>> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<Map<UUID, Integer>>>() {
            });

            EventMetaData metadata = event.getMetadata();
            Map<UUID, Integer> deductionPro = event.getPayload();

            if(deductionPro.values().size() <= 0){
                log.info("PRODUCT-SERVICE: [onMessageHandlerOrderUpSertSuccessPromotion] Promotion data is null");
                ack.acknowledge();
                return SagaStateResponse.success();
            } 
            this.promotionService.spinningQuantity(deductionPro, metadata.getEventId(), false);
            ack.acknowledge();
            return SagaStateResponse.success();
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: onMessageHandlerOrderUpSertSuccessPromotion: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure("Promotion processing failed: " + e.getMessage());
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-promotion-failed}", 
        groupId = "${spring.kafka.group.product}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    @SendTo
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessPromotionFailed(
        @Payload String message,
        Acknowledgment ack,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessPromotionFailed] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessPromotionFailed] Received message payload: {}", message);

            KafkaEvent<Map<UUID, Integer>> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<Map<UUID, Integer>>>() {
            });

            EventMetaData metadata = event.getMetadata();
            Map<UUID, Integer> deductionPro = event.getPayload();

            if(deductionPro.values().size() <= 0){
                log.info("PRODUCT-SERVICE: [onMessageHandlerOrderUpSertSuccessPromotionFailed] Promotion data is null");
                ack.acknowledge();
                return SagaStateResponse.success();
            } 
            this.promotionService.spinningQuantity(deductionPro, metadata.getEventId(), true);
            ack.acknowledge();
            return SagaStateResponse.success();
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: onMessageHandlerOrderUpSertSuccessPromotionFailed: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure("Promotion processing failed: " + e.getMessage());
        }
    }

}
