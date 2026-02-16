package com.fashion.order.service;

public interface RedisDistributedService {
    RedisDistributedLocker getLock(String lockKey);
}
