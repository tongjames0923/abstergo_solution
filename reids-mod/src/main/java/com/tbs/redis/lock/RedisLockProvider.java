package com.tbs.redis.lock;

import com.tbs.common.error.AssertUtil;
import com.tbs.lock.IConditionLock;
import com.tbs.lock.ILock;
import com.tbs.lock.ILockProvider;
import com.tbs.lock.impls.LockAdapter;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;

/**
 * redis锁提供者
 *
 * @author tongj
 */
public class RedisLockProvider implements ILockProvider {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public ILock getLocker(Object target) {
        AssertUtil.notNull(target, "lock target can not be null");
        return new RedisLock(target.toString(), redissonClient);
    }

    @Override
    public IConditionLock getConditionLocker(Object target) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("redis lock not support condition lock");
    }
}
