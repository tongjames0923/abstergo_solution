package com.tbs.boardcast.event.impl;

import com.tbs.boardcast.event.IEvent;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Optional;

/**
 * 默认事件实现
 *
 * @author abstergo
 */
@Slf4j
public class SimpleEvent implements IEvent {
    private static final long serialVersionUID = 1L;
    private String eventName;
    private byte[] eventData;
    private String eventDataClass;
    private int eventPriority = 0;
    private String eventId;

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    public SimpleEvent(String eventId, String eventName) {
        this.eventName = eventName;
        this.eventId = eventId;
    }

    public SimpleEvent() {
        this.eventName = "default";
        this.eventId = "default";
    }

    @Override
    public byte[] getEventData() {
        return eventData;
    }

    @Override
    public void setEventData(byte[] eventData, Class<?> clazz) {
        if (eventData == null) {
            this.eventDataClass = null;
            this.eventData = null;
            return;
        }
        AssertUtil.notNull(eventData, "eventData can not be null");
        AssertUtil.notNull(clazz, "class can not be null");
        this.eventData = eventData;
        this.eventDataClass = clazz.getName();
    }

    @Override
    public Optional<Class<?>> getEventDataClass() {
        if (StrUtil.isEmpty(eventDataClass)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(Class.forName(eventDataClass));
        } catch (ClassNotFoundException e) {
            log.error("class not found", e);
            return Optional.empty();
        }
    }

    @Override
    public int getEventPriority() {
        return eventPriority;
    }
}
