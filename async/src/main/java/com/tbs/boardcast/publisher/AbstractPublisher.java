package com.tbs.boardcast.publisher;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.impl.SpringManagedListenablePublishResult;
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
    protected abstract <T> IPublishResult<T> makePublishResult(IEvent<T> event, HandleMode handleMode);

    @Override
    public <T> IPublishResult<T> publish(IEvent<T> event, HandleMode handleMode) {
        IPublishResult<T> publishResult = makePublishResult(event, handleMode);
        eventBus.inqueueEvent(publishResult);
        if (handleMode == HandleMode.SYNC) {
            publishResult.untilEventComplete();
        }
        return publishResult;
    }

}
