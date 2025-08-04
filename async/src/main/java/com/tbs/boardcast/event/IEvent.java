package com.tbs.boardcast.event;

import com.tbs.boardcast.enums.EventStatusEnum;

import java.io.Serializable;
import java.util.Optional;

/**
 * 广播机制事件接口
 *
 * @author abstergo
 */
public interface IEvent extends Serializable {

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
    byte[] getEventData();

    /**
     * 获取事件数据
     *
     * @param clazz
     * @return
     */
    void setEventData(byte[] eventData, Class<?> clazz);

    /**
     * 获取事件数据
     *
     * @param clazz
     * @return
     */
    Optional<Class<?>> getEventDataClass();

    /**
     * 获取事件优先级
     *
     * @return
     */
    default int getEventPriority() {
        return 0;
    }

}
