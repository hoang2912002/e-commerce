package com.fashion.order.service.impls;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fashion.order.common.enums.EnumError;
import com.fashion.order.exception.KafkaException;
import com.fashion.order.service.KafkaService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate;
    ObjectMapper objectMapper;

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
            log.error("ORDER-SERVICE: SerializationException: {}" + exception.getMessage(), exception);
        }
    }

    @Override
    public <T> CompletableFuture<T> sendAndCallBack(String topic, Object value, Class<T> clazz, Supplier<T> callBack) {
        return this.sendAndCallBack(topic, null, value, clazz, callBack);
    }

    @Override
    public <T> CompletableFuture<T> sendAndCallBack(String topic, Object key, Object value, Class<T> clazz, Supplier<T> callBack) {
        CompletableFuture<T> resultFuture = new CompletableFuture<>();
        try {
            log.info("SAGA-KAFKA: Sending message to topic [{}] with key [{}]", topic, key);
            
            CompletableFuture<SendResult<K, V>> future = kafkaTemplate.send(
                MessageBuilder.withPayload(value)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .build()
            );
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("SAGA-KAFKA: Send successful to topic [{}], partition [{}], offset [{}]", 
                        topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    
                    try {
                        T callbackResult = callBack.get();
                        resultFuture.complete(callbackResult);
                    } catch (Exception callbackEx) {
                        log.error("SAGA-KAFKA: Error executing callback for topic [{}]", topic, callbackEx);
                        resultFuture.completeExceptionally(callbackEx);
                    }
                    
                } else {
                    log.error("SAGA-KAFKA: Send error to topic [{}]. Reason: {}", topic, ex.getMessage(), ex);
                    
                    resultFuture.completeExceptionally(
                        new KafkaException(EnumError.ORDER_KAFKA_REQUEST_TIME_OUT_WITH_BROKER, "kafka.error.request.time.out")
                    );
                }
            });
        } catch (SerializationException exception) {
            log.error("ORDER-SERVICE: SerializationException: {}" + exception.getMessage(), exception);
        }

        return resultFuture;
    }
    
    @Override
    public <T> CompletableFuture<T> sendAndWaitReply(String topic, String replyTopic, Object key, Object value, Class<T> clazz, long timeoutMs){
        CompletableFuture<T> resultFuture = new CompletableFuture<>();
        try {
            log.info("SAGA-KAFKA: [sendAndWaitReply] Sending message to topic [{}] with key [{}]", topic, key);

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                topic,
                key != null ? key.toString() : null,
                value
            );

            // Set reply topic header
            record.headers().add(
                new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes(StandardCharsets.UTF_8))
            );

            RequestReplyFuture<String, Object, Object> replyFuture = 
                replyingKafkaTemplate.sendAndReceive(record, Duration.ofMillis(timeoutMs));
            
            replyFuture.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("SAGA-KAFKA: Request-reply timeout or error for topic [{}]: {}", 
                        topic, ex.getMessage(), ex);
                    resultFuture.completeExceptionally(
                        new KafkaException(
                            EnumError.ORDER_KAFKA_REQUEST_TIME_OUT_WITH_BROKER, 
                            "Saga step timeout: " + ex.getMessage()
                        )
                    );
                } else {
                    try {
                        // ConsumerRecord<String, Object> responseRecord = result.getConsumerRecord();
                        Object responseValue = result.value();
                        
                        log.info("SAGA-KAFKA: Received reply from topic [{}]: {}", topic, responseValue);
                        
                        T response;
                        if (responseValue instanceof String) {
                            response = objectMapper.readValue((String) responseValue, clazz);
                        } else {
                            response = objectMapper.convertValue(responseValue, clazz);
                        }

                        resultFuture.complete(response);
                    } catch (Exception parseEx) {
                        log.error("SAGA-KAFKA: Error parsing reply: {}", parseEx.getMessage(), parseEx);
                        resultFuture.completeExceptionally(parseEx);
                    }
                }
            });
        } catch (Exception e) {
            log.error("SAGA-KAFKA: Error sending request-reply: {}", e.getMessage(), e);
            resultFuture.completeExceptionally(e);
        }
        return resultFuture;
    }
}
