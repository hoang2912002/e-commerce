package com.fashion.product.service;

public interface RedisDistributedService {
    RedisDistributedLocker getLock(String lockKey);
}
