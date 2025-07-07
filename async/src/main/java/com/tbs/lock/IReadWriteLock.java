package com.tbs.lock;

/**
 * IReadWriteLock 接口定义了读写锁的基本操作。
 *
 * @author Abstergo
 */
public interface IReadWriteLock {
    /**
     * 获取一个读锁。
     * @return
     */
    ILock readLock();

    /**
     * 获取一个写锁。
     * @return
     */
    ILock writeLock();
}

