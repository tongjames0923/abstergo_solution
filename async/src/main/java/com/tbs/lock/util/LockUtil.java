package com.tbs.lock.util;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.lock.IConditionLock;
import com.tbs.lock.ILock;
import com.tbs.lock.ILockProvider;
import com.tbs.lock.config.LockProperty;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;

/**
 * @author abstergo
 */
@Component
public class LockUtil {

    /**
     * -- GETTER -- 获取锁工具实例
     *
     * @return
     */
    @Getter
    private static LockUtil instance = null;

    @Resource
    private ILockProvider lockProvider;

    @Resource
    private LockProperty lockProperty;

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 获取锁
     *
     * @param lockName
     * @return
     */
    public ILock getLockerByStr(String lockName) {
        return lockProvider.getLocker(lockName);
    }

    public IConditionLock getConditionLockByStr(String lockName) {
        return lockProvider.getConditionLocker(lockName);
    }

    /**
     * 获取锁
     *
     * @param target
     * @return
     */
    public ILock getLockerByObj(Object target) {
        if (target instanceof String) {
            return getLockerByStr((String)target);
        }
        return lockProvider.getLocker(target);
    }

    /**
     * 获取锁
     *
     * @param target
     * @return
     */
    public IConditionLock getConditionLockByObj(Object target) {
        if (target instanceof String) {
            return getConditionLockByStr((String)target);
        }
        return lockProvider.getConditionLocker(target);
    }

    /**
     * 获取锁
     *
     * @param clazz
     * @return
     */
    public ILock getLockerByClass(Class<?> clazz) {
        return lockProvider.getLocker(clazz.getPackageName() + "." + clazz.getSimpleName());
    }

    /**
     * 获取锁
     *
     * @param clazz
     * @return
     */
    public IConditionLock getConditionLockByClass(Class<?> clazz) {
        return lockProvider.getConditionLocker(clazz.getPackageName() + "." + clazz.getSimpleName());
    }

    /**
     * 获取锁并执行
     *
     * @param lockName
     * @param runnable
     */
    public void lockThen(String lockName, Runnable runnable) {
        lockThen(getLockerByStr(lockName), runnable);
    }

    /**
     * 获取锁并执行
     *
     * @param target
     * @param runnable
     */
    public void lockThen(Object target, Runnable runnable) {
        lockThen(getLockerByObj(target), runnable);
    }

    public void lockThen(Class<?> clazz, Runnable runnable) {
        lockThen(getLockerByClass(clazz), runnable);
    }

    /**
     * 获取锁并执行
     *
     * @param lock
     * @param runnable
     */
    public void lockThen(ILock lock, Runnable runnable) {
        if (lock.tryLock(Duration.ofMillis(lockProperty.getAccquireTimeout()))) {
            try {
                runnable.run();
            } finally {
                lock.unLock();
            }
        } else {
            AssertUtil.throwError(() -> new FormattedRuntimeException("锁{} 加锁失败", lock.toString()));
        }
    }

}
