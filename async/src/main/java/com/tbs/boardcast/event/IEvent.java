package com.tbs.boardcast.event;

import com.tbs.boardcast.enums.EventStatusEnum;

/**
 * 广播机制事件接口
 *
 * @author abstergo
 */
public interface IEvent<T> {

    /**
     * 获取事件ID
     *
     * @return
     */
    String getEventId();

    /**
     * 获取事件名称
     *
     * @return
     */
    String getEventName();

    /**
     * 获取事件数据
     *
     * @return
     */
    T getEventData();

    /**
     * 设置事件数据
     *
     * @param eventData
     */
    void setEventData(T eventData);

    /**
     * 获取事件优先级
     *
     * @return
     */
    default int getEventPriority() {
        return 0;
    }

}
