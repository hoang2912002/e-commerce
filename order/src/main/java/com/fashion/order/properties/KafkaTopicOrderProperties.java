package com.fashion.order.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Configuration
@Data
@ConfigurationProperties("spring.kafka.topic.order")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KafkaTopicOrderProperties {
    String orderCreatedSuccessPayment;
    String orderCreatedSuccessShipping;
    String orderCreatedSuccessInventory;
    String orderCreatedSuccessPromotion;
    String orderCreatedSuccessPaymentFailed;
    String orderCreatedSuccessShippingFailed;
    String orderCreatedSuccessInventoryFailed;
    String orderCreatedSuccessPromotionFailed;
    String orderCreatedSuccessReply;
}
