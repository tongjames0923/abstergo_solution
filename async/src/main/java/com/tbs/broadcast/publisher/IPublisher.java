package com.tbs.broadcast.publisher;

import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;

/**
 * 发布者接口
 *
 * @author abstergo
 */
public interface IPublisher {
    /**
     * 获取发布者ID
     *
     * @return
     */
    String getPushlisherId();

    /**
     * 发布
     *
     * @param event
     * @param handleMode
     * @return
     */
    IPublishResult publish(IEvent event, HandleMode handleMode);

    /**
     * 同步发布
     *
     * @param event
     * @return
     */
    default IPublishResult pushlishSync(IEvent event) {
        return publish(event, com.tbs.broadcast.enums.HandleMode.SYNC);
    }

    /**
     * 异步发布
     *
     * @param event
     * @return
     */
    default IPublishResult pushlishAsync(IEvent event) {
        return publish(event, HandleMode.ASYNC);
    }

}
