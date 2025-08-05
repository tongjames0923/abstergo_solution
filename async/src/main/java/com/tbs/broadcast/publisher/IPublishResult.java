package com.tbs.broadcast.publisher;

import com.tbs.broadcast.enums.EventStatusEnum;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;

/**
 * 发布结果
 *
 * @author abstergo
 */
public interface IPublishResult {
    /**
     * 获取事件
     *
     * @return
     */
    IEvent getEvent();

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

    /**
     * 设置事件状态
     *
     * @param eventStatus
     */
    void setEventStatus(EventStatusEnum eventStatus);

    /**
     * 获取事件状态
     *
     * @return
     */
    EventStatusEnum getEventStatus();

}
