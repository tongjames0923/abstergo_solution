package com.tbs.boardcast.publisher;

import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;

/**
 * 发布结果
 *
 * @author abstergo
 */
public interface IPublishResult<T> {
    /**
     * 获取事件
     *
     * @return
     */
    IEvent<T> getEvent();

    /**
     * 获取发布者
     *
     * @return
     */
    IPublisher getPublisher();

    /**
     * 获取处理模式
     *
     * @return
     */
    HandleMode getHandleMode();

    /**
     * 获取异常
     *
     * @return
     */
    Throwable getThrowable();

    /**
     * 设置异常
     *
     * @param throwable
     */
    void setThrowable(Throwable throwable);
}
