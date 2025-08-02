package com.tbs.boardcast.dispatcher.impl;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.subscriber.AbstractSubscriber;
import com.tbs.common.IStartup;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.error.AssertUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * 默认事件分发器
 *
 * @author abstergo
 */
public abstract class AbstractEventDispatcher implements IEventDispatcher, IStartup, DisposableBean {

    @Resource
    private IEventBus eventBus;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractEventDispatcher.class);

    private boolean started = false;

    @Getter
    @Setter
    private IDataHolder subscribersHolder;

    public AbstractEventDispatcher(IDataHolder subscribersHolder) {
        this.subscribersHolder = subscribersHolder;
    }

    @Override
    public void register(AbstractSubscriber subscriber) {
        AssertUtil.notNull(subscriber, "订阅者不能为空");
        getSubscribersHolder().set(subscriber.getSubscriberId(), subscriber, true);
    }

    @Override
    public void unregister(AbstractSubscriber subscriber) {
        AssertUtil.notNull(subscriber, "订阅者不能为空");
        getSubscribersHolder().remove(subscriber.getSubscriberId());
    }

    @Override
    public IEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void handleEvent() {
        if (started) {
            return;
        }
        started = true;
        logger.trace("开始处理事件");
        while (started) {
            IPublishResult<?> iPublishResult = null;
            try {
                iPublishResult = getEventBus().nextUnhandledEvent(Duration.ofMinutes(15));
                if (iPublishResult == null) {
                    logger.trace("空事件超时");
                    continue;
                }
                for (AbstractSubscriber subscriber : getSubscribersHolder().values().stream().filter((v) -> {
                    return v != null && v instanceof AbstractSubscriber;
                }).map(v -> (AbstractSubscriber)v).collect(Collectors.toList())) {
                    if (subscriber.supportEvent(iPublishResult.getEvent().getEventId(),
                        iPublishResult.getEvent().getEventName()) &&
                        subscriber.supportPublisher(iPublishResult.getPublisher())) {
                        if (iPublishResult.getHandleMode() == HandleMode.SYNC) {
                            subscriber.recive(iPublishResult, iPublishResult.getPublisher());
                        } else {
                            subscriber.reciveAsync(iPublishResult, iPublishResult.getPublisher());
                        }
                    }
                }
            } catch (Throwable e) {
                handleError(e, iPublishResult);
            }
        }
        logger.trace("停止处理事件");

    }

    @Override
    public void stopHandle() {
        started = false;
    }

    @Override
    @LongTermAsync
    public void startUp() throws RuntimeException {
        handleEvent();
    }

    @Override
    public void destroy() throws Exception {
        stopHandle();
    }
}
