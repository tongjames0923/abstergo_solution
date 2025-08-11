package com.tbs.broadcast.dispatcher.impl;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.subscriber.AbstractSubscriber;
import com.tbs.common.IStartup;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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

    private Map<String, AbstractSubscriber> subscribersHolder = new HashMap<>(8);

    @Override
    public void register(AbstractSubscriber subscriber) {
        AssertUtil.notNull(subscriber, "订阅者不能为空");
        subscribersHolder.put(subscriber.getSubscriberId(), subscriber);
    }

    @Override
    public void unregister(AbstractSubscriber subscriber) {
        AssertUtil.notNull(subscriber, "订阅者不能为空");
        subscribersHolder.remove(subscriber.getSubscriberId());
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
            ValueWrapper<IPublishResult> iPublishResult = new ValueWrapper<>();
            try {
                iPublishResult.set(getEventBus().nextUnhandledEvent(Duration.ofMinutes(15)));
                if (iPublishResult == null) {
                    logger.trace("空事件超时");
                    continue;
                }
                iPublishResult.get().ifPresent(res -> {
                    subscribersHolder.values().stream().filter((v) -> {
                        return v != null && v instanceof AbstractSubscriber;
                    }).map(v -> (AbstractSubscriber)v).forEach(subscriber -> {
                        if (subscriber.supportEvent(res.getEvent().getEventId(), res.getEvent().getEventName()) &&
                            subscriber.supportPublisher(res.getPublisher())) {
                            if (res.getHandleMode() == HandleMode.SYNC) {
                                subscriber.recive(res);
                            } else {
                                subscriber.reciveAsync(res, res.getPublisher());
                            }
                        }
                    });
                });

            } catch (Throwable e) {
                handleError(e, iPublishResult.get().orElse(null));
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
