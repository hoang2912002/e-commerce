package com.fashion.product.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
@ConfigurationProperties("spring.kafka.topic.product")
public class KafkaTopicApprovalHistoryProperties {
    String approvalHistoryApprovedProductSuccess;
}
