package com.fashion.product.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Configuration
@Data
@ConfigurationProperties("spring.kafka.topic.product")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KafkaTopicShopManagementProperties {
    String shopManagementCreatedSuccess;
}
