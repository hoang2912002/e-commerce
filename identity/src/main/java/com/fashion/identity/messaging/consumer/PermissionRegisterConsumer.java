package com.fashion.identity.messaging.consumer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.identity.dto.response.kafka.EventMetaData;
import com.fashion.identity.dto.response.kafka.KafkaEvent;
import com.fashion.identity.dto.response.kafka.PermissionRegisteredEvent;
import com.fashion.identity.entity.Permission;
import com.fashion.identity.entity.Role;
import com.fashion.identity.repository.PermissionRepository;
import com.fashion.identity.repository.RoleRepository;
import com.fashion.identity.service.PermissionService;
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
public class PermissionRegisterConsumer {
    PermissionService permissionService;
    PermissionRepository permissionRepository;
    RoleRepository roleRepository;
    // ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topic.identity.permission-register}", groupId = "${spring.kafka.group.permission}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessageHandlerPermissionRegister(
            @Payload String message) {
        try {
            log.info("[onMessageHandler] Start consuming message ...");
            log.info("[onMessageHandler] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();

            KafkaEvent<List<PermissionRegisteredEvent>> event = objectMapper.readValue(
                    message,
                    new TypeReference<KafkaEvent<List<PermissionRegisteredEvent>>>() {
                    });
            // Lấy Metadata ở đây:
            EventMetaData metadata = event.getMetadata();

            List<PermissionRegisteredEvent> payload = event.getPayload();

            List<Permission> permissionsDB = this.permissionRepository.findAllByService(metadata.getSource());

            Set<String> existingKeys = permissionsDB.stream()
                    .map(p -> p.getApiPath() + ":" + p.getMethod() + ":" + p.getModule())
                    .collect(Collectors.toSet());

            List<Permission> newPermissions = payload.stream()
                    .filter(p -> !existingKeys.contains(p.getApiPath() + ":" + p.getMethod() + ":" + p.getModule()))
                    .map(p -> 
                        Permission.builder()
                        .apiPath(p.getApiPath())
                        .name(p.getName())
                        .method(p.getMethod())
                        .module(p.getModule())
                        .service(metadata.getSource())
                        .build()
                    )
                    .collect(Collectors.toList());

            if (!newPermissions.isEmpty()) {
                // Role roleDB = this.roleRepository.findBySlug("admin");
                // roleDB.getPermissions().clear();
                // roleDB.getPermissions().addAll(permissionsDB);
                // roleRepository.save(roleDB);
                this.permissionRepository.saveAll(newPermissions);
            } else {
                log.info("IDENTITY-SERVICE: onMessageHandlerPermissionRegister No new permissions found, everything is up-to-date: {}", metadata.getSource());
            }
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: onMessageHandlerPermissionRegister: {}", e.getMessage());
        }
    }
}
