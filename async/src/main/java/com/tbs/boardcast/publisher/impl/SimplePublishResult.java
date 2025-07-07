package com.tbs.boardcast.publisher.impl;

import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.publisher.IPublisher;

import java.io.Serializable;

/**
 * 简单的PublishResult实现
 *
 * @author abstergo
 */
public class SimplePublishResult<T> implements IPublishResult<T>, Serializable {
    private static final long serialVersionUID = 1L;
    private IEvent<T> event;
    private IPublisher publisher;
    private HandleMode handleMode;
    private Throwable throwable;

    public SimplePublishResult(IEvent<T> event, IPublisher publisher, HandleMode handleMode) {
        this.event = event;
        this.publisher = publisher;
        this.handleMode = handleMode;
    }

    public SimplePublishResult() {
    }

    @Override
    public IEvent<T> getEvent() {
        return event;
    }

    @Override
    public IPublisher getPublisher() {
        return publisher;
    }

    @Override
    public HandleMode getHandleMode() {
        return handleMode;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        getEvent().setEventStatus(EventStatusEnum.FAILED);
        this.throwable = throwable;
    }
}
