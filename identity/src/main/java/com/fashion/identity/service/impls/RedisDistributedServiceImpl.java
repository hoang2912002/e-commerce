package com.fashion.identity.service.impls;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.fashion.identity.service.RedisDistributedLocker;
import com.fashion.identity.service.RedisDistributedService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisDistributedServiceImpl implements RedisDistributedService{
    RedissonClient redissonClient;

    @Override
    public RedisDistributedLocker getLock(String lockKey) {
        return new RedisDistributedLocker() {
            RLock lock = redissonClient.getLock(lockKey);
            @Override
            // leaseTime là thời gian "thuê" khóa. Sau khoảng thời gian này, khóa sẽ tự động được giải phóng.
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                return this.lock.tryLock(waitTime, leaseTime, unit);
            }

            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                this.lock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                if (isLocked() && isHeldByCurrentThread()) {
                    this.lock.unlock();
                }
            }

            @Override
            public boolean isLocked() {
                return this.lock.isLocked();
            }

            @Override
            public boolean isHeldByThread(long threadId) {
                return this.lock.isHeldByThread(threadId);
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return this.lock.isHeldByCurrentThread();
            }

            @Override
            public String getName() {
                return this.lock.getName();
            }
            
        };
    }
    
}
