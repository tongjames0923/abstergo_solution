package com.tbs.async;

/**
 * 线程异常处理
 *
 * @author abstergo
 */
public interface IOnThreadError {
    /**
     * 线程异常处理
     *
     * @param throwable 异常
     * @param at        线程
     */
    void onThreadError(Throwable throwable, Thread at);
}
