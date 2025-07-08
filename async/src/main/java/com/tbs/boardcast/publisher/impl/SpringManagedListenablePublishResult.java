package com.tbs.boardcast.publisher.impl;

import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.AbstractPublishResult;
import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.common.util.SpringUtil;

import java.util.Collection;
import java.util.List;

/**
 * 由于Spring管理的监听器的发布结果实现
 *
 * @author tongj
 */
public class SpringManagedListenablePublishResult<T> extends AbstractPublishResult<T> {

    public SpringManagedListenablePublishResult(IEvent<T> event, IPublisher publisher, HandleMode handleMode) {
        super(event, publisher, handleMode);
    }

    @Override
    protected Collection<IEventStatusListenner> getStatusListeners() {
        return SpringUtil.getBeansOfType(IEventStatusListenner.class).values();
    }
}
