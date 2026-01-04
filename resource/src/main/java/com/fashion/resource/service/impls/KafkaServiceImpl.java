package com.fashion.resource.service.impls;

import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fashion.resource.service.KafkaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaServiceImpl<K, V> implements KafkaService {
    KafkaTemplate<K, V> kvKafkaTemplate;

    @Override
    public void send(String topic, Object value) {
        this.send(topic, null, value);
    }

    @Override
    public void send(String topic, Object key, Object value) {
        try {
            kvKafkaTemplate.send(
                    MessageBuilder.withPayload(value)
                            .setHeader(KafkaHeaders.TOPIC, topic)
                            .setHeader(KafkaHeaders.KEY, key)
                            .build()
            );
        } catch (SerializationException exception) {
            log.error("RESOURCE-SERVICE: SerializationException: {}" + exception.getMessage(), exception);
        }
    }
}
