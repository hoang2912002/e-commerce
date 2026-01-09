package com.fashion.notification.messaging.consumer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.notification.dto.response.kafka.EventMetaData;
import com.fashion.notification.dto.response.kafka.KafkaEvent;
import com.fashion.notification.dto.response.kafka.UserRegisterEvent;
import com.fashion.notification.dto.response.kafka.UserVerifyCodeEvent;
import com.fashion.notification.service.EmailService;
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
public class IdentityServiceConsumer {
    EmailService emailService;

    @KafkaListener(topics = "${spring.kafka.topic.identity.user-created}", groupId = "${spring.kafka.group.identity}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessageHandlerSendMailVerifyCode(@Payload String message){
        try {
            log.info("[onMessageHandlerSendMailVerifyCode] Start consuming message ...");
            log.info("[onMessageHandlerSendMailVerifyCode] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();

            KafkaEvent<UserVerifyCodeEvent> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<UserVerifyCodeEvent>>() {
            });

            EventMetaData metadata = event.getMetadata();
            UserVerifyCodeEvent userData = event.getPayload();

            if(userData.getId() instanceof UUID && Objects.nonNull(userData.getVerifyCode())){
                this.emailService.sendMailVerifyCode(userData);
            } else {
                log.info("NOTIFICATION-SERVICE: [onMessageHandlerSendMailVerifyCode] Incorrect format user data receive message from kafka: {}", metadata.getSource());
            }
        } catch (Exception e) {
            log.error("NOTIFICATION-SERVICE: onMessageHandlerSendMailVerifyCode: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.identity.user-created-success}", groupId = "${spring.kafka.group.identity}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessageHandlerSendMailUserRegister(@Payload String message){
        try {
            log.info("[onMessageHandlerSendMailUserRegister] Start consuming message ...");
            log.info("[onMessageHandlerSendMailUserRegister] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();

            KafkaEvent<UserRegisterEvent> event = objectMapper.readValue(
                message,
                new TypeReference<KafkaEvent<UserRegisterEvent>>() {
            });

            EventMetaData metadata = event.getMetadata();
            UserRegisterEvent userData = event.getPayload();

            if(userData.getId() instanceof UUID){
                this.emailService.sendMailUserRegister(userData);
            } else {
                log.info("NOTIFICATION-SERVICE: [onMessageHandlerSendMailUserRegister] Incorrect format user data receive message from kafka: {}", metadata.getSource());
            }
        } catch (Exception e) {
            log.error("NOTIFICATION-SERVICE: onMessageHandlerSendMailUserRegister: {}", e.getMessage());
        }
    }
}
