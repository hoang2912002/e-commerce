package com.fashion.inventory.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
@ConfigurationProperties("spring.kafka.topic.inventory")
public class KafkaTopicInventoryServiceProperties {
    String inventoryCreatedSuccess;
    String inventoryCreationFailed;
}
