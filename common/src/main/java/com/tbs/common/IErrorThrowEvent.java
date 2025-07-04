package com.tbs.common;

/**
 * 经过AssertUtil抛出的异常处理事件
 *
 * @author tongj
 */
public interface IErrorThrowEvent {
    /**
     * 当AssertUtil抛出异常时调用
     *
     * @param throwable
     * @return 是否继续抛出异常
     */
    boolean onAssertUtilThrow(Throwable throwable);
}
