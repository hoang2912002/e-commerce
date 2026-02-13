package com.fashion.payment.service;

public interface RedisDistributedService {
    RedisDistributedLocker getLock(String lockKey);
}
