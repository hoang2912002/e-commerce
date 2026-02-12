package com.fashion.inventory.service;

public interface RedisDistributedService {
    RedisDistributedLocker getLock(String lockKey);
}
