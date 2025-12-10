package com.tbs.lock.redis;

import com.tbs.lock.ILock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redisson实现的Redis分布式锁
 * <p>
 * 实现ILock接口，提供分布式锁的获取、释放和状态检查功能
 * 使用Redisson客户端的RLock实现分布式锁逻辑
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RedissonLock implements ILock {

    private RedissonClient client;

    private RLock rLock;

    /**
     * 构造方法
     *
     * @param lockKey 锁的键名
     * @param client Redisson客户端实例
     */
    public RedissonLock(String lockKey, RedissonClient client) {
        this.rLock = client.getLock(lockKey);
        this.client = client;
    }

    /**
     * 尝试获取锁
     * <p>
     * 在指定的超时时间内尝试获取锁，如果获取成功返回true，否则返回false
     * 如果线程在等待过程中被中断，将设置中断标志并返回false
     * </p>
     *
     * @param timeOut 超时时间
     * @return 是否成功获取锁
     */
    @Override
    public boolean tryLock(Duration timeOut) {
        try {
            return rLock.tryLock(timeOut.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 检查当前线程是否持有锁
     *
     * @return 当前线程是否持有锁
     */
    @Override
    public boolean isHeldByCurrentThread() {
        return rLock.isHeldByCurrentThread();
    }

    /**
     * 检查锁是否被任何线程持有
     *
     * @return 锁是否被持有
     */
    @Override
    public boolean isLocked() {
        return rLock.isLocked();
    }

    /**
     * 释放锁
     * <p>
     * 如果当前线程持有锁，则释放锁；否则可能抛出异常
     * </p>
     */
    @Override
    public void unLock() {
        rLock.unlock();
    }

}
