package com.fashion.product.service;

public interface KafkaService {
    public void send(String topic, Object value);
    public void send(String topic, Object key, Object value);
    public void sendAndCallBack(String topic, Object value);
    public void sendAndCallBack(String topic, Object key, Object value);
}
