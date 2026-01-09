package com.fashion.notification.messaging.producer.impls;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fashion.notification.common.enums.EventType;
import com.fashion.notification.dto.response.kafka.EventMetaData;
import com.fashion.notification.dto.response.kafka.KafkaEvent;
import com.fashion.notification.dto.response.kafka.PermissionRegisteredEvent;
import com.fashion.notification.messaging.producer.IdentityProvider;
import com.fashion.notification.properties.KafkaTopicIdentityProperties;
import com.fashion.notification.service.KafkaService;
import com.fashion.notification.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdentityProviderImpl implements IdentityProvider{
    KafkaTopicIdentityProperties kafkaTopicIdentityProperties;
    KafkaService kafkaService;
    PermissionService permissionService;

    @Override
    // @Scheduled(
    //    initialDelay = 30_000, // chờ app start xong
    //    fixedDelay = 3600000 // 1 giờ
    // )
    public void permissionRegisterEventIdentity() {
        var topic = kafkaTopicIdentityProperties.getPermissionRegister();
        log.info("RESOURCE-SERVICE: permissionRegisterEventIdentity(): permission register to topic {}", topic);

        // Get all endpoint
        List<PermissionRegisteredEvent> permissions = permissionService.listEndPoints();

        if (permissions.isEmpty()) return;

        KafkaEvent<List<PermissionRegisteredEvent>> message = KafkaEvent.<List<PermissionRegisteredEvent>>builder()
            .metadata(EventMetaData.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.PERMISSION_REGISTER.name())
                .source("notification-service")
                .version(1)
                .build())
            .payload(permissions)
            .build();
        kafkaService.sendAndCallBack(topic, message);
    }
    
}
