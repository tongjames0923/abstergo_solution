package com.tbs.broadcast.publisher;

import com.tbs.broadcast.enums.EventStatusEnum;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;
import com.tbs.common.error.AssertUtil;

import java.util.Collection;

/**
 * 抽象发布结果
 *
 * @author tongj
 */
public abstract class AbstractPublishResult implements IPublishResult {

    /**
     * 获取事件状态监听器
     *
     * @return
     */
    protected abstract Collection<IEventStatusListenner> getStatusListeners();

    /**
     * 根据发布者id获取发布者
     *
     * @param publisherId
     * @return
     */
    protected abstract IPublisher getPublisherById(String publisherId);

    private EventStatusEnum eventStatus = EventStatusEnum.IDLE;

    private IEvent event;
    private String publisherId;
    private HandleMode handleMode;
    private Throwable throwable;

    public AbstractPublishResult(IEvent event, String publisher, HandleMode handleMode) {
        AssertUtil.notNull(event, "event can not be null");
        AssertUtil.hasText(publisher, "publisher can not be null");
        AssertUtil.notNull(handleMode, "handleMode can not be null");
        this.event = event;
        this.publisherId = publisher;
        this.handleMode = handleMode;
    }

    public AbstractPublishResult() {
    }

    @Override
    public IEvent getEvent() {
        return event;
    }

    @Override
    public IPublisher getPublisher() {
        return getPublisherById(publisherId);
    }

    @Override
    public HandleMode getHandleMode() {
        return handleMode;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * 设置异常, 设置事件状态为重试
     *
     * @param throwable
     */
    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        getStatusListeners().forEach(listenner -> listenner.onEventHandleError(throwable, this));
    }

    private boolean isEventComplete() {
        return getEventStatus().isTerminal();
    }

    @Override
    public void setEventStatus(EventStatusEnum eventStatus) {
        AssertUtil.notNull(eventStatus, "eventStatus can not be null");
        AssertUtil.isTrue(isEventComplete(), "can not set event status to " +
            eventStatus.getValue() +
            " when event status is " +
            getEventStatus().getValue());
        getStatusListeners().forEach(listenner -> listenner.onEventStatusChange(eventStatus, getEventStatus(), this));
        this.eventStatus = eventStatus;
    }

    @Override
    public EventStatusEnum getEventStatus() {
        return eventStatus;
    }

}
