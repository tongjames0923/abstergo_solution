package com.tbs.boardcast.bus.impl;

import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.schedule.IScheduleTask;
import com.tbs.schedule.impl.DelayedTaskWrapper;

import java.util.Optional;

/**
 * 延迟事件发布结果
 *
 * @author tongj
 */
public class DelayEventPublishResult<T> extends DelayedTaskWrapper implements IPublishResult<T> {
    private Optional<IPublishResult<T>> publishResult;

    public DelayEventPublishResult(IScheduleTask scheduleTask, IPublishResult<T> publishResult) {
        super(scheduleTask);
        this.publishResult = Optional.ofNullable(publishResult);
    }

    @Override
    public IEvent<T> getEvent() {
        return publishResult.map((r) -> {
            return r.getEvent();
        }).orElse(null);
    }

    @Override
    public IPublisher getPublisher() {
        return publishResult.map((r) -> {
            return r.getPublisher();
        }).orElse(null);
    }

    @Override
    public HandleMode getHandleMode() {
        return publishResult.map((r) -> {
            return r.getHandleMode();
        }).orElse(null);
    }

    @Override
    public Throwable getThrowable() {
        return publishResult.map((r) -> {
            return r.getThrowable();
        }).orElse(null);
    }

    @Override
    public void setThrowable(Throwable throwable) {
        publishResult.ifPresent((r) -> {
            r.setThrowable(throwable);
        });
    }

    @Override
    public EventStatusEnum untilEventComplete() {
        return publishResult.map((r) -> {
            return r.untilEventComplete();
        }).orElse(EventStatusEnum.IDLE);
    }

    @Override
    public void setEventStatus(EventStatusEnum eventStatus) {
        publishResult.ifPresent((r) -> {
            r.setEventStatus(eventStatus);
        });
    }

    @Override
    public EventStatusEnum getEventStatus() {
        return publishResult.map((r) -> {
            return r.getEventStatus();
        }).orElse(EventStatusEnum.IDLE);
    }

}
