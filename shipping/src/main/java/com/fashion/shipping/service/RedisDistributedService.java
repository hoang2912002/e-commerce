package com.fashion.shipping.service;

public interface RedisDistributedService {
    RedisDistributedLocker getLock(String lockKey);
}
