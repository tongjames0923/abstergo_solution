package com.tbs.broadcast.subscriber;

import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.IPublisher;

/**
 * 接收处理接口
 *
 * @author tongj
 */
public interface IReceiveHandler {
    /**
     * 接收处理
     *
     * @param event
     * @param publisher
     * @return 接收处理结果根据 {@link AbstractSubscriber}中的常量变量定义
     */
    int recive(IEvent event, IPublisher publisher);
}
