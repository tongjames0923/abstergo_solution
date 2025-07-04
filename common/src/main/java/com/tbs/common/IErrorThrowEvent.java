package com.tbs.common;

/**
 * 经过AssertUtil抛出的异常处理事件
 *
 * @author tongj
 */
public interface IErrorThrowEvent {
    /**
     * 处理异常
     *
     * @param throwable
     * @return 是否继续抛出异常
     */
    boolean handle(Throwable throwable);
}
