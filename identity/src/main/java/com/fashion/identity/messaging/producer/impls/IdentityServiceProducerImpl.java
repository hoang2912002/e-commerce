package com.fashion.identity.messaging.producer.impls;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fashion.identity.common.enums.EventType;
import com.fashion.identity.dto.response.kafka.EventMetaData;
import com.fashion.identity.dto.response.kafka.KafkaEvent;
import com.fashion.identity.dto.response.kafka.PermissionRegisteredEvent;
import com.fashion.identity.dto.response.kafka.UserRegisterEvent;
import com.fashion.identity.dto.response.kafka.UserVerifyCodeEvent;
import com.fashion.identity.messaging.producer.IdentityServiceProducer;
import com.fashion.identity.properties.KafkaTopicIdentityProperties;
import com.fashion.identity.service.KafkaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityServiceProducerImpl implements IdentityServiceProducer{
    KafkaService kafkaService;
    KafkaTopicIdentityProperties kafkaTopicIdentityProperties;
    @Override
    public void produceUserEventSuccess(UserVerifyCodeEvent userData) {
        // Send mail after create user successful
        var topic = kafkaTopicIdentityProperties.getUserCreated();
        log.info("IDENTITY-SERVICE: produceUserEventSuccess(): user create successful to send mail verify code topic {}", topic);

        
        KafkaEvent<UserVerifyCodeEvent> message = KafkaEvent.<UserVerifyCodeEvent>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.USER_CREATED.name())
                .source("identity-service")
                .version(1)
                .build())
            .payload(userData)
            .build();
        kafkaService.send(topic, message);
    }
    
    @Override
    public void produceUserVerifyEventSuccess(UserRegisterEvent userData) {
        var topic = kafkaTopicIdentityProperties.getUserCreatedSuccess();
        log.info("IDENTITY-SERVICE: produceUserVerifyEventSuccess(): user create successful to send mail notification welcome join topic {}", topic);

        KafkaEvent<UserRegisterEvent> message = KafkaEvent.<UserRegisterEvent>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.USER_CREATED.name())
                .source("identity-service")
                .version(1)
                .build())
            .payload(userData)
            .build();
        kafkaService.send(topic, message);
    }
    
}
