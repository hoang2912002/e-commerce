package com.fashion.identity.messaging.consumer;

import java.util.List;
import java.util.Objects;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fashion.identity.dto.response.kafka.EventMetaData;
import com.fashion.identity.dto.response.kafka.KafkaEvent;
import com.fashion.identity.dto.response.kafka.PermissionRegisteredEvent;
import com.fashion.identity.dto.response.kafka.ShopManagementAddressEvent;
import com.fashion.identity.entity.Address;
import com.fashion.identity.service.AddressService;
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
public class ProductServiceConsumer {
    AddressService addressService;
    
    @KafkaListener(topics = "${spring.kafka.topic.product.shop-management-created-success}", groupId = "${spring.kafka.group.product}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessageHandlerShopManagementCreated(@Payload String message) {
        try {
            log.info("[onMessageHandlerShopManagementCreated] Start consuming message ...");
            log.info("[onMessageHandlerShopManagementCreated] Received message payload: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();
            KafkaEvent<ShopManagementAddressEvent> event = objectMapper.readValue(
                    message,
                    new TypeReference<KafkaEvent<ShopManagementAddressEvent>>() {
                    });
            // Lấy Metadata ở đây:
            EventMetaData metadata = event.getMetadata();
            ShopManagementAddressEvent data = event.getPayload();
            if(Objects.nonNull(data)){
                this.addressService.upSertShopManagementAddress(Address.builder()
                    .address(data.getAddress())
                    .district(data.getDistrict())
                    .ward(data.getWard())
                    .province(data.getProvince())
                    .shopManagementId(data.getShopManagementId())
                    .build());
            } else{
                log.info("IDENTITY-SERVICE: onMessageHandlerShopManagementCreated No found shop management address: {}", metadata.getSource());
            }
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: onMessageHandlerShopManagementCreated: {}", e.getMessage());
        }
    }
}
