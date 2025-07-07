package com.tbs.lock;

/**
 * 锁提供者
 *
 * @author abstergo
 */
public interface ILockProvider {
    /**
     * 获取锁
     *
     * @param target
     * @return
     */
    ILock getLocker(Object target);
}
