package com.tbs.redis.lock;

import com.tbs.lock.ILock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * Redis锁
 *
 * @author tongj
 */
public class RedisLock implements ILock {

    private RedissonClient client;

    private RLock rLock;

    public RedisLock(String lockKey, RedissonClient client) {
        this.rLock = client.getLock(lockKey);
        this.client = client;
    }

    @Override
    public boolean tryLock(Duration timeOut) {
        try {
            return rLock.tryLock(timeOut.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return rLock.isHeldByCurrentThread();
    }

    @Override
    public boolean isLocked() {
        return rLock.isLocked();
    }

    @Override
    public void unLock() {
        rLock.unlock();
    }

}
