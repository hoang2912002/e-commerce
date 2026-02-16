package com.fashion.order.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface KafkaService {
    public void send(String topic, Object value);
    public void send(String topic, Object key, Object value);
    public <T> CompletableFuture<T> sendAndCallBack(String topic, Object value, Class<T> clazz, Supplier<T> callBack);
    public <T> CompletableFuture<T> sendAndCallBack(String topic, Object key, Object value,  Class<T> clazz, Supplier<T> callBack);
    public <T> CompletableFuture<T> sendAndWaitReply(String topic, String replyTopic, Object key, Object value,  Class<T> clazz, long timeoutMs);
}
