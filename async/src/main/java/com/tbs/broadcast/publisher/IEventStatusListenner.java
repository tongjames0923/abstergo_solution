package com.tbs.broadcast.publisher;

import com.tbs.broadcast.enums.EventStatusEnum;

/**
 * 事件状态监听器
 *
 * @param <T>
 */
public interface IEventStatusListenner<T> {
    /**
     * 事件状态改变
     *
     * @param oldEventStatus
     * @param eventStatus
     * @param publishResult
     */
    void onEventStatusChange(EventStatusEnum eventStatus, EventStatusEnum oldEventStatus, IPublishResult publishResult);

    /**
     * 事件处理异常
     *
     * @param throwable
     * @param publishResult
     */
    void onEventHandleError(Throwable throwable, IPublishResult publishResult);
}
