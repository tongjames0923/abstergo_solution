package com.tbs.lock;

import java.time.Duration;
import java.util.concurrent.locks.Condition;

/**
 * 锁
 *
 * @author abstergo
 */
public interface ILock {

    /**
     * 尝试获取锁
     *
     * @param timeOut
     * @return
     */
    boolean tryLock(Duration timeOut);

    /**
     * 是否被当前线程上锁
     *
     * @return
     */
    boolean isHeldByCurrentThread();

    /**
     * 是否已经上锁，无论是不是当前线程。
     *
     * @return
     */
    boolean isLocked();

    /**
     * 释放锁
     */
    void unLock();

    /**
     * 创建条件变量
     *
     * @return
     */
    Condition newCondition();

}
