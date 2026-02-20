package com.fashion.shipping.service.provider;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.fashion.shipping.dto.response.VersionResponse;
import com.fashion.shipping.service.RedisDistributedLocker;
import com.fashion.shipping.service.RedisDistributedService;
import com.fashion.shipping.service.RedisService;
import com.google.common.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CacheProvider {
    Cache<String, Object> localCache;
    RedisService redisService;
    RedisDistributedService redisDistributedService;
    public <T extends VersionResponse> T getDataResponse(String key, String lockKey,  Long version, Class<T> clazz, Supplier<T> dbFallback) {
        // 1. Get Local Cache if null return immediately
        Object rawData = localCache.getIfPresent(key);
        if (rawData instanceof Optional && ((Optional<?>) rawData).isEmpty()) {
            return null; 
        }
        // 2. Sure having data
        if (rawData != null) {
            T localData = clazz.cast(rawData);
            if (isDataFresh(localData, version)) {
                log.info("Get data local cache: {}", key);
                return localData;
            }
        }
        // 3. Get Data from Redis
        return getRedisDataResponse(key, lockKey, version, clazz, dbFallback);
    }

    private <T extends VersionResponse> T getRedisDataResponse(String key, String lockKey, Long version, Class<T> clazz, Supplier<T> dbFallback){
        // 1. Get all redisson lock
        RedisDistributedLocker distributedLock = redisDistributedService.getLock(lockKey);
        try {
            //2. Lock redis by key
            boolean isDistributedLock = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isDistributedLock) {
                return null;
            }
            T redisData = redisService.getObject(key, clazz);
            if (redisData != null && isDataFresh(redisData, version)) {
                localCache.put(key, redisData);
                log.info("Get data redis cache: {}", key);
                return redisData;
            }
            //3. DB call back get data
            T dbData = dbFallback.get();
            log.info("Get data DB: {}", key);
            if (dbData != null) {
                this.put(key, dbData);
            }
            else {
                redisService.setObjectNull(key, null, 60L, TimeUnit.SECONDS);
                localCache.put(key, Optional.empty());
            }
            return dbData;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (redisDistributedService.getLock(lockKey).isLocked() && redisDistributedService.getLock(lockKey).isHeldByCurrentThread()) {
                redisDistributedService.getLock(lockKey).unlock();
            }
        }
        return null;
    }

    private <T extends VersionResponse> boolean isDataFresh(T data, Long requiredVersion) {
        if (requiredVersion == null) return true;
        return data.getVersion() >= requiredVersion;
    }

    public void put(String key, Object data) {
        redisService.setObject(key, data);
        localCache.put(key, data);
    }
}
