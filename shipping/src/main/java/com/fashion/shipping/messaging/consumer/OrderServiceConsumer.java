package com.fashion.shipping.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.fashion.shipping.common.enums.ShippingEnum;
import com.fashion.shipping.dto.response.ShippingResponse;
import com.fashion.shipping.dto.response.kafka.EventMetaData;
import com.fashion.shipping.dto.response.kafka.KafkaEvent;
import com.fashion.shipping.dto.response.kafka.SagaStateResponse;
import com.fashion.shipping.service.ShippingService;
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
    ShippingService shippingService;
    ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-shipping}", 
        groupId = "${spring.kafka.group.shipping}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    @SendTo
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessShipping(
        @Payload String message,
        Acknowledgment ack,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessShipping] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessShipping] Received message payload: {}", message);

            KafkaEvent<ShippingResponse> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<ShippingResponse>>() {
            });

            EventMetaData metadata = event.getMetadata();
            ShippingResponse shippingReq = event.getPayload();

            if(shippingReq == null){
                log.info("PRODUCT-SERVICE: [onMessageHandlerOrderUpSertSuccessShipping] Shipping data is null");
                ack.acknowledge();
                return SagaStateResponse.failure(
                    null,
                    ShippingEnum.FAILED,
                    "Invalid shipping request"
                );
            } 
            SagaStateResponse response = this.shippingService.commandShipping(shippingReq, metadata.getEventId());
            ack.acknowledge();
            return response;

        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: onMessageHandlerOrderUpSertSuccessShipping: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure(
                null,
                ShippingEnum.FAILED,
                "Processing failed: " + e.getMessage()
            );
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-shipping-failed}", 
        groupId = "${spring.kafka.group.shipping}", 
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    @SendTo
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessShippingFailed(
        @Payload String message,
        Acknowledgment ack,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessShippingFailed] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessShippingFailed] Received message payload: {}", message);

            KafkaEvent<ShippingResponse> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<ShippingResponse>>() {
            });

            EventMetaData metadata = event.getMetadata();
            ShippingResponse shippingReq = event.getPayload();

            if (shippingReq != null) {
                shippingService.compensateShipping(shippingReq, metadata.getEventId());
            }
            ack.acknowledge();
            return SagaStateResponse.success();

        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: onMessageHandlerOrderUpSertSuccessShipping: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure("Shipping processing failed: " + e.getMessage());
        }
    }
}
