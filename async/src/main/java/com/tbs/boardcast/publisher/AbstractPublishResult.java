package com.tbs.boardcast.publisher;

import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.common.util.AssertUtil;
import com.tbs.common.util.SpringUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象发布结果
 *
 * @author tongj
 */
public abstract class AbstractPublishResult<T> implements IPublishResult<T> {

    /**
     * 获取事件状态监听器
     *
     * @return
     */
    protected abstract Collection<IEventStatusListenner> getStatusListeners();

    private AtomicBoolean isEventComplete = new AtomicBoolean(false);
    private EventStatusEnum eventStatus = EventStatusEnum.IDLE;
    private CompletableFuture<EventStatusEnum> eventFuture = new CompletableFuture<>();

    /**
     * 事件状态监听器
     *
     * @param <T>
     */
    public static interface IEventStatusListenner<T> {
        /**
         * 事件状态改变
         *
         * @param oldEventStatus
         * @param eventStatus
         * @param publishResult
         */
        void onEventStatusChange(EventStatusEnum eventStatus, EventStatusEnum oldEventStatus,
            IPublishResult<T> publishResult);

        /**
         * 事件处理异常
         *
         * @param throwable
         * @param publishResult
         */
        void onEventHandleError(Throwable throwable, IPublishResult<T> publishResult);
    }

    private IEvent<T> event;
    private IPublisher publisher;
    private HandleMode handleMode;
    private Throwable throwable;

    public AbstractPublishResult(IEvent<T> event, IPublisher publisher, HandleMode handleMode) {
        AssertUtil.notNull(event, "event can not be null");
        AssertUtil.notNull(publisher, "publisher can not be null");
        AssertUtil.notNull(handleMode, "handleMode can not be null");
        this.event = event;
        this.publisher = publisher;
        this.handleMode = handleMode;
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
        if (isEventComplete()) {
            eventFuture.complete(getEventStatus());
        }
    }

    @Override
    public EventStatusEnum getEventStatus() {
        return eventStatus;
    }

    @Override
    public EventStatusEnum untilEventComplete() {
        return eventFuture.join();
    }
}
