package com.tbs.broadcast.subscriber;

import com.tbs.broadcast.enums.EventStatusEnum;
import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.thread.IThreadPoolManager;
import com.tbs.thread.impl.SimpleThreadRequest;

import javax.annotation.Resource;

/**
 * 订阅者
 *
 * @author abstergo
 */
public abstract class AbstractSubscriber {

    @Resource
    IThreadPoolManager threadPoolManager;

    /**
     * 订阅者ID
     *
     * @return
     */
    public abstract String getSubscriberId();

    /**
     * 是否支持该事件
     *
     * @param eventId
     * @param eventName
     * @return
     */
    public abstract boolean supportEvent(String eventId, String eventName);

    /**
     * 是否支持该发布者
     *
     * @param publisher
     * @return
     */
    public abstract boolean supportPublisher(IPublisher publisher);

    /**
     * 接收事件
     *
     * @param event
     * @param publisher
     */
    protected abstract void onReceive(IEvent event, IPublisher publisher);

    /**
     * 接收事件
     *
     * @param event
     * @param publisher
     * @return
     */
    public void recive(IPublishResult event, IPublisher publisher) {
        try {
            onReceive(event.getEvent(), publisher);
            event.setEventStatus(EventStatusEnum.DONE);
        } catch (Throwable e) {
            event.setThrowable(e);
        }
    }

    /**
     * 异步接收事件
     *
     * @param event
     * @param publisher
     * @return
     */
    public void reciveAsync(IPublishResult event, IPublisher publisher) {
        threadPoolManager.submit(SimpleThreadRequest.Builder.create("AbstractSubscriber-" + getSubscriberId(), () -> {
            recive(event, publisher);
        }).build(), null);
    }
}
