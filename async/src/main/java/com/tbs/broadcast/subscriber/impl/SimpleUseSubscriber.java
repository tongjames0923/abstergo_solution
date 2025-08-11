package com.tbs.broadcast.subscriber.impl;

import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.broadcast.subscriber.AbstractSubscriber;
import com.tbs.broadcast.subscriber.IReceiveHandler;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 简易使用订阅者
 *
 * @author tongj
 */
public class SimpleUseSubscriber extends AbstractSubscriber implements IStartup {
    private String subscriberId;
    private boolean supportAnyEventId;
    private boolean supportAnyEventName;
    private Set<String> supportEventIds;
    private Set<String> supportEventNames;

    private IReceiveHandler eventHandler;

    public static class Builder {
        private String subscriberId;
        private boolean supportAnyEventId = true;
        private boolean supportAnyEventName = true;
        private Set<String> supportEventIds;
        private Set<String> supportEventNames;
        private IReceiveHandler eventHandler;

        /**
         * 创建一个自动订阅者
         *
         * @param subscriberId 订阅者id
         * @param eventHandler 事件处理函数
         * @return 订阅者
         */
        public static Builder newBuilder(String subscriberId, @NotNull IReceiveHandler eventHandler) {
            AssertUtil.notNull(eventHandler, "eventHandler can not be null");
            AssertUtil.isTrue(StrUtil.isEmpty(subscriberId), "subscriberId can not be empty");
            Builder builder = new Builder();
            builder.subscriberId = subscriberId;
            builder.eventHandler = eventHandler;
            return builder;
        }

        /**
         * 支持指定事件id
         *
         * @param eventIds 事件id
         * @return 订阅者
         */
        public Builder supportEventIds(@NotNull Collection<String> eventIds) {
            if (CollUtil.isNotEmpty(eventIds)) {
                this.supportAnyEventId = false;
                this.supportEventIds = eventIds.stream().collect(Collectors.toSet());
            }

            return this;
        }

        /**
         * 支持指定事件名称
         *
         * @param eventNames 事件名称
         * @return 订阅者
         */
        public Builder supportEventNames(@NotNull Collection<String> eventNames) {
            if (CollUtil.isNotEmpty(eventNames)) {
                this.supportAnyEventName = false;
                this.supportEventNames = eventNames.stream().collect(Collectors.toSet());
            }
            return this;
        }

        public SimpleUseSubscriber build() {
            SimpleUseSubscriber subscriber =
                new SimpleUseSubscriber(eventHandler, supportEventNames, supportEventIds, supportAnyEventName,
                    supportAnyEventId, subscriberId);
            return subscriber;
        }
    }

    protected SimpleUseSubscriber(@NotNull IReceiveHandler eventHandler, Set<String> supportEventNames,
        Set<String> supportEventIds, boolean supportAnyEventName, boolean supportAnyEventId,
        @NotNull String subscriberId) {
        this.eventHandler = eventHandler;
        this.subscriberId = subscriberId;

        this.supportEventNames = supportEventNames == null ? new HashSet<>(1) : supportEventNames;
        this.supportEventIds = supportEventIds == null ? new HashSet<>(1) : supportEventIds;
        this.supportAnyEventName = supportAnyEventName;
        this.supportAnyEventId = supportAnyEventId;

    }

    @Resource
    private IEventDispatcher dispatcher;

    @Override
    public String getSubscriberId() {
        return subscriberId;
    }

    @Override
    public boolean supportEvent(String eventId, String eventName) {
        return (supportAnyEventId || supportEventIds.contains(eventId)) &&
            (supportAnyEventName || supportEventNames.contains(eventName));
    }

    @Override
    public boolean supportPublisher(IPublisher publisher) {
        return true;
    }

    @Override
    protected int onReceive(IEvent event, IPublisher publisher) {
        return eventHandler.recive(event, publisher);
    }

    @Override
    public void startUp() throws RuntimeException {
        dispatcher.register(this);
    }
}
