package com.tbs.lock;

import java.util.concurrent.locks.Condition;

/**
 * 锁接口
 *
 * @author tongj
 */
public interface IConditionLock extends ILock {
    /**
     * 创建条件变量
     *
     * @return
     */
    Condition newCondition();
}
