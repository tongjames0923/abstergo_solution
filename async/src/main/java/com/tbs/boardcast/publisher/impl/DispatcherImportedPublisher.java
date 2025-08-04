package com.tbs.boardcast.publisher.impl;

import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.AbstractPublisher;
import com.tbs.boardcast.publisher.IPublishResult;
import lombok.Getter;

/**
 * 通过外部导入分发器的发布者
 *
 * @author abstergo
 */

public class DispatcherImportedPublisher extends AbstractPublisher {

    /**
     * 获取分发器
     *
     * @return
     */
    @Getter
    private IEventDispatcher eventDispatcher;

    public DispatcherImportedPublisher(String publisherId, IEventDispatcher eventDispatcher) {
        super(publisherId, eventDispatcher.getEventBus());
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    protected IPublishResult makePublishResult(IEvent event, HandleMode handleMode) {
        return new SpringManagedListenablePublishResult(event, this.getPushlisherId(), handleMode);
    }
}
