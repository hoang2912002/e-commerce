package com.fashion.resource.service;

public interface KafkaService<K, V> {
    void send(final String topic, V value);
    void send(final String topic, K key, V value);
}
