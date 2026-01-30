package com.fashion.payment.messaging.consumer;

import java.lang.reflect.Field;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.payment.dto.response.PaymentResponse.InnerInternalPayment;
import com.fashion.payment.dto.response.kafka.EventMetaData;
import com.fashion.payment.dto.response.kafka.KafkaEvent;
import com.fashion.payment.dto.response.kafka.OrderCreatedEvent;
import com.fashion.payment.service.PaymentService;
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
    PaymentService paymentService;
    @KafkaListener(topics = "${spring.kafka.topic.order.order-created-success}", groupId = "${spring.kafka.group.payment}", containerFactory = "kafkaListenerContainerFactory")
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

            if(orderData.getPayment() != null){
                this.paymentService.upSertPayment(orderData.getPayment(), metadata.getEventId());
            } else {
                log.info("PRODUCT-SERVICE: [onMessageHandlerOrderUpSertSuccess] Not found promotion data received message from kafka with sender ORDER-SERVICE: {}", metadata.getSource());
            }
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: onMessageHandlerOrderUpSertSuccess: {}", e.getMessage());
        }
    }
}
