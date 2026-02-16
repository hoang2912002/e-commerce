package com.fashion.payment.messaging.consumer;

import java.lang.reflect.Field;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.fashion.payment.dto.response.PaymentResponse.InnerInternalPayment;
import com.fashion.payment.dto.response.kafka.EventMetaData;
import com.fashion.payment.dto.response.kafka.KafkaEvent;
import com.fashion.payment.dto.response.kafka.OrderCreatedEvent;
import com.fashion.payment.dto.response.kafka.SagaStateResponse;
import com.fashion.payment.entity.Payment;
import com.fashion.payment.exception.ServiceException;
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
    ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-payment}", 
        groupId = "${spring.kafka.group.payment}", 
        containerFactory = "kafkaListenerContainerFactory", 
        concurrency = "3"
    )
    @SendTo // Enable automatic reply from header
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessPayment(
        @Payload String message,
        Acknowledgment ack,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessPayment] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessPayment] Received message payload: {}", message);

            KafkaEvent<InnerInternalPayment> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<InnerInternalPayment>>() {
            });

            EventMetaData metadata = event.getMetadata();
            InnerInternalPayment paymentData = event.getPayload();

            if (paymentData == null) {
                log.warn("PAYMENT-SERVICE: Payment data is null");
                ack.acknowledge();
                return SagaStateResponse.failure("Payment data is null");
            }

            SagaStateResponse response = paymentService.upSertPayment(
                paymentData, 
                metadata.getEventId(), 
                true // isSuccess
            );
            
            log.info("PAYMENT-SERVICE: Payment processed, replying with success={}", response.isSuccess());
            ack.acknowledge();
            return response;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: onMessageHandlerOrderUpSertSuccessPayment: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure("Payment processing failed: " + e.getMessage());
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.order.order-created-success-payment-failed}", 
        groupId = "${spring.kafka.group.payment}", 
        containerFactory = "kafkaListenerContainerFactory", 
        concurrency = "3"
    )
    @SendTo // Enable automatic reply from header
    public SagaStateResponse onMessageHandlerOrderUpSertSuccessPaymentFailed(
        @Payload String message,
        Acknowledgment ack,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(value = KafkaHeaders.REPLY_TOPIC, required = false) byte[] replyTo
    ){
        try {
            log.info("[onMessageHandlerOrderUpSertSuccessPaymentFailed] Start consuming message ...");
            log.info("[onMessageHandlerOrderUpSertSuccessPaymentFailed] Received message payload: {}", message);

            KafkaEvent<InnerInternalPayment> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<InnerInternalPayment>>() {
            });

            EventMetaData metadata = event.getMetadata();
            InnerInternalPayment paymentData = event.getPayload();

            if(paymentData != null){
                this.paymentService.upSertPayment(paymentData, metadata.getEventId(), false);
                ack.acknowledge();
                return SagaStateResponse.builder()
                    .success(true)
                    .paymentId(paymentData.getId())
                    .paymentStatus(paymentData.getStatus())
                    .build();
            } else {
                log.info("PAYMENT-SERVICE: [onMessageHandlerOrderUpSertSuccessPaymentFailed] Payment data is null");
                ack.acknowledge();
                return SagaStateResponse.failure("Payment data is null");
            }
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: onMessageHandlerOrderUpSertSuccessPaymentFailed: {}", e.getMessage());
            ack.acknowledge();
            return SagaStateResponse.failure("Payment processing failed: " + e.getMessage());
        }
    }
}
