package com.fashion.notification.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Configuration
@Data
@ConfigurationProperties("spring.kafka.topic.identity")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KafkaTopicIdentityProperties {
    String permissionRegister;
}
