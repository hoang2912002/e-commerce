package com.fashion.notification.service;

public interface KafkaService {
    void send(String topic, Object value);
    void send(String topic, Object key, Object value);
    void sendAndCallBack(String topic, Object value);
    void sendAndCallBack(String topic, Object key, Object value);
}
