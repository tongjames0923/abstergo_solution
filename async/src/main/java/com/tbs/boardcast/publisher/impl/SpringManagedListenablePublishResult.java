package com.tbs.boardcast.publisher.impl;

import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import com.tbs.boardcast.publisher.AbstractPublishResult;
import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.common.util.SpringUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 由于Spring管理的监听器的发布结果实现
 *
 * @author tongj
 */
public class SpringManagedListenablePublishResult extends AbstractPublishResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 缓存发布者
     */
    private static final Map<String, IPublisher> pubMap = new ConcurrentHashMap<>(8);

    public SpringManagedListenablePublishResult(IEvent event, String publisher, HandleMode handleMode) {
        super(event, publisher, handleMode);
    }

    public SpringManagedListenablePublishResult() {
        super();
    }

    @Override
    protected Collection<IEventStatusListenner> getStatusListeners() {
        return SpringUtil.getBeansOfType(IEventStatusListenner.class).values();
    }

    @Override
    protected IPublisher getPublisherById(String publisherId) {
        return pubMap.computeIfAbsent(publisherId, (k) -> {
            return SpringUtil.getBeansOfType(IPublisher.class).values().stream()
                .filter(p -> p != null && publisherId.equals(p.getPushlisherId())).findFirst()
                .orElseThrow(() -> new RuntimeException("未找到发布者:" + publisherId));
        });
    }
}
