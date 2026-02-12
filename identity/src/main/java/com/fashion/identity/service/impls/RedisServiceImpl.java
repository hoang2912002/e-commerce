package com.fashion.identity.service.impls;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fashion.identity.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisServiceImpl implements RedisService{
    RedisTemplate<String, Object> redisTemplate;
    @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper;
    
    
    @Override
    public String getString(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(String::valueOf)
                .orElse(null);
        } catch (Exception e) {
            log.error("Error getting string from Redis for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void setString(String key, String value) {
        if (!StringUtils.isNotBlank(key)) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Error setting string in Redis for key {}: {}", key, e.getMessage());
        }
    }

    @Override
    public <T> T getObject(String key, Class<T> targetClass) {
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) {
            return null;
        }
        try {
            // ObjectMapper objectMapper = new ObjectMapper();
            return redisObjectMapper.convertValue(obj, targetClass);
        } catch (Exception e) {
            log.error("Error converting object from Redis for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void setObject(String key, Object value) {
        if (!StringUtils.isNotBlank(key)) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Error setting object in Redis for key {}: {}", key, e.getMessage());
        }
    }

    @Override
    public Integer getInteger(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.valueOf((String) value);
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting integer from Redis for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void setInteger(String key, Integer value) {
        if (StringUtils.isNotBlank(key)) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Error setting integer in Redis for key {}: {}", key, e.getMessage());
        }
    }

    @Override
    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error deleting key {} from Redis: {}", key, e.getMessage());
        }
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    public void setObjectNull(String key, Object value, Long time, TimeUnit timeUnit) {
        if (StringUtils.isNotBlank(key)) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
        } catch (Exception e) {
            log.error("Error setting object in Redis for key {}: {}", key, e.getMessage());
        }
    }
}
