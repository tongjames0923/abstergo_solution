package com.tbs.lock.impls.providers;

import com.tbs.lock.IConditionLock;
import com.tbs.lock.ILock;
import com.tbs.lock.ILockProvider;
import com.tbs.lock.impls.LockAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Abstergo
 */
public class JdkLockProvider implements ILockProvider {
    private Map<Object, LockAdapter> lockMap = new ConcurrentHashMap<>(8);

    @Override
    public ILock getLocker(Object target) {
        return getConditionLocker(target);
    }

    @Override
    public IConditionLock getConditionLocker(Object target) {
        return lockMap.computeIfAbsent(target, k -> new LockAdapter(new java.util.concurrent.locks.ReentrantLock()));
    }
}
