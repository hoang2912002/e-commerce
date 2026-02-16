package com.fashion.order.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

public interface RedisService {
    String getString(String key);
    void setString(String key, String value);
    <T> T getObject(String key, Class<T> targetClass);
    void setObject(String key, Object value);
    Integer getInteger(String key);
    void setInteger(String key, Integer value);
    void deleteKey(String key);
    RedisTemplate<String, Object> getRedisTemplate();
    void setObjectNull(String key, Object value, Long time, TimeUnit timeUnit);
}
