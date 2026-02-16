package com.fashion.order.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import com.fashion.order.properties.KafkaTopicOrderProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaTopicOrderProperties kafkaTopicOrderProperties;

    @Value("${spring.kafka.group.order-reply-saga}")
    String groupSagaOrder;
    
    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
        ProducerFactory<String, Object> producerFactory,
        ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory
    ){
        String replyTopic = kafkaTopicOrderProperties.getOrderCreatedSuccessReply();
        ConcurrentMessageListenerContainer<String, Object> replyContainer = containerFactory.createContainer(replyTopic);
        replyContainer.getContainerProperties().setGroupId(groupSagaOrder);
        
        return new ReplyingKafkaTemplate<>(producerFactory, replyContainer);
    }
}