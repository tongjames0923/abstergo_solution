package com.tbs.boardcast.dispatcher;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.subscriber.AbstractSubscriber;

import java.util.concurrent.ExecutorService;

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
     * 获取执行器
     *
     * @return
     */
    ExecutorService getExecutorService();

    /**
     * 获取事件总线
     *
     * @return
     */
    IEventBus getEventBus();

    /**
     * 处理事件
     */
    void handleEvent();

    /**
     * 停止处理事件
     */
    void stopHandle();

}
