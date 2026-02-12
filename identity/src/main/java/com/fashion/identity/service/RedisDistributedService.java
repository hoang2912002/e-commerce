package com.fashion.identity.service;

public interface RedisDistributedService {
    RedisDistributedLocker getLock(String lockKey);
}
