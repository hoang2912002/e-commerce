package com.fashion.resource.messaging.provider.impls;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fashion.resource.common.enums.EventType;
import com.fashion.resource.dto.response.PermissionResponse.InnerPermissionResponse;
import com.fashion.resource.dto.response.kafka.EventMetaData;
import com.fashion.resource.dto.response.kafka.KafkaEvent;
import com.fashion.resource.dto.response.kafka.KafkaPermissionRegisterResponse;
import com.fashion.resource.messaging.provider.IdentityProvider;
import com.fashion.resource.properties.KafkaTopicProperties;
import com.fashion.resource.service.EndpointScannerService;
import com.fashion.resource.service.KafkaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityProviderImpl implements IdentityProvider{
    KafkaTopicProperties kafkaTopicProperties;
    KafkaService kafkaService;
    EndpointScannerService scanner;

    @Scheduled(
       initialDelay = 30_000, // chờ app start xong
       fixedDelay = 3600000 // 1 giờ
    )
    @Override
    public void permissionRegisterEventIdentity(){
        var topic = kafkaTopicProperties.getPermissionRegister();
        log.info("RESOURCE-SERVICE: produceOrderEventSuccess(): producing order to topic {}", topic);

        // Get all endpoint
        List<KafkaPermissionRegisterResponse> permissions = scanner.listPermission();

        if (permissions.isEmpty()) return;

        KafkaEvent<List<KafkaPermissionRegisterResponse>> message = KafkaEvent.<List<KafkaPermissionRegisterResponse>>builder()
                .metadata(EventMetaData.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(EventType.PERMISSION_REGISTER.name())
                        .source("resource-service")
                        .version(1)
                        .build())
                .payload(permissions)
                .build();
        kafkaService.send(topic, message);

    }
}
