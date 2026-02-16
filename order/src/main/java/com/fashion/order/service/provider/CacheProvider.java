package com.fashion.order.service.provider;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.fashion.order.dto.response.VersionResponse;
import com.fashion.order.service.RedisDistributedLocker;
import com.fashion.order.service.RedisDistributedService;
import com.fashion.order.service.RedisService;
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

    public <I, T extends VersionResponse> Map<I, T> getDataResponseBatch(
        Set<I> ids,
        Function<I, String> keyGenerator,
        Function<Set<I>, String> batchLockKeyGenerator,
        Long version,
        Class<T> clazz,
        Function<Set<I>, Map<I, T>> dbBatchFallback
    ) {
        Map<I, T> result = new ConcurrentHashMap<>();
        Set<I> missingIds = new HashSet<>();
        
        // 1. Check local cache first
        for (I id : ids) {
            String cacheKey = keyGenerator.apply(id);
            Object rawData = localCache.getIfPresent(cacheKey);
            
            if (rawData instanceof Optional && ((Optional<?>) rawData).isEmpty()) {
                continue; // Skip null markers
            }
            
            if (rawData != null) {
                T localData = clazz.cast(rawData);
                if (isDataFresh(localData, version)) {
                    result.put(id, localData);
                    continue;
                }
            }
            missingIds.add(id);
        }
        
        if (missingIds.isEmpty()) {
            return result;
        }
        
        // 2. Batch lock for missing IDs
        String batchLockKey = batchLockKeyGenerator.apply(missingIds);
        RedisDistributedLocker lock = redisDistributedService.getLock(batchLockKey);
        
        try {
            if (!lock.tryLock(1, 5, TimeUnit.SECONDS)) {
                // Another thread is loading, try Redis once more
                for (I id : missingIds) {
                    String cacheKey = keyGenerator.apply(id);
                    T redisData = redisService.getObject(cacheKey, clazz);
                    if (redisData != null && isDataFresh(redisData, version)) {
                        result.put(id, redisData);
                        localCache.put(cacheKey, redisData);
                    }
                }
                return result;
            }
            
            // 3. Double-check Redis after acquiring lock
            Set<I> stillMissing = new HashSet<>();
            for (I id : missingIds) {
                String cacheKey = keyGenerator.apply(id);
                T redisData = redisService.getObject(cacheKey, clazz);
                if (redisData != null && isDataFresh(redisData, version)) {
                    result.put(id, redisData);
                    localCache.put(cacheKey, redisData);
                } else {
                    stillMissing.add(id);
                }
            }
            
            // 4. Fetch from DB and cache SYNCHRONOUSLY
            if (!stillMissing.isEmpty()) {
                Map<I, T> dbData = dbBatchFallback.apply(stillMissing);
                for (Map.Entry<I, T> entry : dbData.entrySet()) {
                    String cacheKey = keyGenerator.apply(entry.getKey());
                    this.put(cacheKey, entry.getValue());
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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