package com.tbs.boardcast.publisher.impl;

import com.tbs.boardcast.dispatcher.IEventDispatcher;
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

}
