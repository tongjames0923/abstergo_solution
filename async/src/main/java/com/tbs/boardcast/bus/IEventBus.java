package com.tbs.boardcast.bus;

import com.tbs.boardcast.publisher.IPublishResult;

import java.time.Duration;

/**
 * 事件总线
 *
 * @author abstergo
 */
public interface IEventBus {

    /**
     * 添加事件到队列
     *
     * @param publishResult
     */
    void inqueueEvent(IPublishResult publishResult);

    /**
     * 获取下一个未处理的事件
     *
     * @param timeOut 超时时间
     * @return
     */
    IPublishResult nextUnhandledEvent(Duration timeOut);

    /**
     * 获取事件队列大小
     *
     * @return
     */
    int getEventQueueSize();

}
