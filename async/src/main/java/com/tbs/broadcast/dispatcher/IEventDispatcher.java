package com.tbs.broadcast.dispatcher;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.subscriber.AbstractSubscriber;

/**
 * 事件分发器
 *
 * @author abstergo
 */
public interface IEventDispatcher {

    /**
     * 注册订阅者
     *
     * @param subscriber
     */
    void register(AbstractSubscriber subscriber);

    /**
     * 注销订阅者
     *
     * @param subscriber
     */
    void unregister(AbstractSubscriber subscriber);

    /**
     * 获取事件总线
     *
     * @return
     */
    IEventBus getEventBus();

    /**
     * 处理事件处理异常
     *
     * @param throwable
     * @param publishResult
     */
    <T> void handleError(Throwable throwable, IPublishResult publishResult);

    /**
     * 处理事件
     */
    void handleEvent();

    /**
     * 停止处理事件
     */
    void stopHandle();

}
