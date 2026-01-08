package com.fashion.notification.service.impls;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.KafkaClient;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fashion.notification.service.KafkaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaServiceImpl<K, V> implements KafkaService {
    KafkaTemplate<K, V> kafkaTemplate;

    @Override
    public void send(String topic, Object value) {
        this.send(topic, null, value);
    }

    @Override
    public void send(String topic, Object key, Object value) {
        try {
            kafkaTemplate.send(
                MessageBuilder.withPayload(value)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .build()
            );
        } catch (SerializationException exception) {
            log.error("NOTIFICATION-SERVICE: SerializationException: {}" + exception.getMessage(), exception);
        }
    }
    
    @Override
    public void sendAndCallBack(String topic, Object value) {
        this.sendAndCallBack(topic, null, value);
    }

    @Override
    public void sendAndCallBack(String topic, Object key, Object value) {
        try {
            CompletableFuture<SendResult<K, V>> future = kafkaTemplate.send(
                MessageBuilder.withPayload(value)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .build()
            );
        } catch (SerializationException exception) {
            log.error("NOTIFICATION-SERVICE: SerializationException: {}" + exception.getMessage(), exception);
        }
    }

    
}
