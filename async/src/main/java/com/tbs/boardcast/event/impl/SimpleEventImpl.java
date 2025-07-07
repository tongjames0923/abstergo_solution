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
    private EventStatusEnum eventStatus = EventStatusEnum.IDLE;
    private int eventPriority = 0;
    private AtomicBoolean isEventComplete = new AtomicBoolean(false);
    private CompletableFuture<EventStatusEnum> eventFuture = new CompletableFuture<>();

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

    private boolean isEventComplete() {
        return getEventStatus() == EventStatusEnum.CANCELED ||
            getEventStatus() == EventStatusEnum.FAILED ||
            getEventStatus() == EventStatusEnum.DONE;
    }

    @Override
    public void setEventStatus(EventStatusEnum eventStatus) {
        AssertUtil.notNull(eventStatus, "eventStatus can not be null");
        AssertUtil.isTrue(isEventComplete(), "can not set event status to " +
            eventStatus.getValue() +
            " when event status is " +
            getEventStatus().getValue());
        this.eventStatus = eventStatus;
        if (isEventComplete()) {
            eventFuture.complete(getEventStatus());
        }
    }

    @Override
    public EventStatusEnum getEventStatus() {
        return eventStatus;
    }

    @Override
    public int getEventPriority() {
        return eventPriority;
    }

    @Override
    public EventStatusEnum untilEventComplete() {
        return eventFuture.join();
    }
}
