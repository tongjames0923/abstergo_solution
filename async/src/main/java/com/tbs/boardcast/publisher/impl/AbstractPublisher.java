package com.tbs.boardcast.publisher.impl;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.common.util.AssertUtil;

/**
 * @author abstergo
 */
public abstract class AbstractPublisher implements IPublisher {
    private String publisherId;
    private IEventBus eventBus;

    public AbstractPublisher(String publisherId, IEventBus eventBus) {
        AssertUtil.hasText(publisherId, "发布者ID不能为空");
        this.publisherId = publisherId;
        this.eventBus = eventBus;
    }

    @Override
    public String getPushblisherId() {
        return publisherId;
    }

    @Override
    public <T> IPublishResult<T> publish(IEvent<T> event, HandleMode handleMode) {
        SimplePublishResult<T> publishResult = new SimplePublishResult<>(event, this, handleMode);
        eventBus.inqueueEvent(publishResult);
        if (handleMode == HandleMode.SYNC) {
            publishResult.getEvent().untilEventComplete();
        }
        return publishResult;
    }

}
