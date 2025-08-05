package com.tbs.broadcast.publisher;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;
import com.tbs.common.error.AssertUtil;

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
    public String getPushlisherId() {
        return publisherId;
    }

    /**
     * 创建发布结果
     *
     * @param event
     * @param handleMode
     * @param <T>
     * @return
     */
    protected abstract IPublishResult makePublishResult(IEvent event, HandleMode handleMode);

    @Override
    public IPublishResult publish(IEvent event, HandleMode handleMode) {
        IPublishResult publishResult = makePublishResult(event, handleMode);
        eventBus.inqueueEvent(publishResult);
        return publishResult;
    }

}
