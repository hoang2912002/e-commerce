package com.fashion.payment.properties;

import lombok.Data;
@Data
public class BaseCacheProperties<T> {
    private String prefix;
    private T keys;
    private String lock;

    public String createCacheKey(String keyPath, Object identifier) {
        return String.format("%s:%s:%s", prefix, keyPath, identifier.toString());
    }

    public String createLockKey(String keyPath, Object identifier) {
        return String.format("%s:%s:%s", lock, keyPath, identifier.toString());
    }
}

