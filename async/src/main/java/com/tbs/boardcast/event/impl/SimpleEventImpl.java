package com.tbs.boardcast.event.impl;

import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.event.IEvent;
import com.tbs.common.util.AssertUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认事件实现
 *
 * @author abstergo
 */
public class SimpleEventImpl<T> implements IEvent<T> {

    private String eventId;
    private String eventName;
    private T eventData;

    private int eventPriority = 0;

    public SimpleEventImpl(String eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
    }

    public SimpleEventImpl() {
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public T getEventData() {
        return eventData;
    }

    @Override
    public void setEventData(T eventData) {
        this.eventData = eventData;
    }

    @Override
    public int getEventPriority() {
        return eventPriority;
    }

}
