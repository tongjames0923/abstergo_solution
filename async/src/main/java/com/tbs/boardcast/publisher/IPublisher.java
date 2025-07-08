package com.tbs.boardcast.publisher;

import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;

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
    <T> IPublishResult<T> publish(IEvent<T> event, HandleMode handleMode);

    /**
     * 同步发布
     *
     * @param event
     * @return
     */
    default IPublishResult pushlishSync(IEvent<?> event) {
        return publish(event, HandleMode.SYNC);
    }

    /**
     * 异步发布
     *
     * @param event
     * @return
     */
    default IPublishResult pushlishAsync(IEvent<?> event) {
        return publish(event, HandleMode.ASYNC);
    }


}
