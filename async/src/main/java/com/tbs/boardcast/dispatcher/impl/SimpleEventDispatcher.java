package com.tbs.boardcast.dispatcher.impl;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.subscriber.AbstractSubscriber;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 默认事件分发器
 *
 * @author abstergo
 */
public class SimpleEventDispatcher implements IEventDispatcher, InitializingBean {

    @Resource
    private IEventBus eventBus;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleEventDispatcher.class);

    private ExecutorService executorService;
    private boolean started = false;

    private Map<String, AbstractSubscriber> subscriberMap = new HashMap<>();

    /**
     * 构造函数
     *
     * @param executorService
     */
    public SimpleEventDispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void register(AbstractSubscriber subscriber) {
        subscriberMap.put(subscriber.getSubscriberId(), subscriber);
    }

    @Override
    public void unregister(AbstractSubscriber subscriber) {
        subscriberMap.remove(subscriber.getSubscriberId());
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
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
        getExecutorService().execute(() -> {
            while (started) {

                IPublishResult<?> iPublishResult = getEventBus().nextUnhandledEvent(Duration.ofMinutes(1));
                if (iPublishResult == null) {
                    continue;
                }
                for (AbstractSubscriber subscriber : subscriberMap.values()) {
                    if (subscriber.supportEvent(iPublishResult.getEvent().getEventName()) &&
                        subscriber.supportPublisher(iPublishResult.getPublisher())) {
                        if (iPublishResult.getHandleMode() == HandleMode.SYNC) {
                            subscriber.recive(iPublishResult, iPublishResult.getPublisher());
                        } else {
                            subscriber.reciveAsync(iPublishResult, iPublishResult.getPublisher());
                        }
                    }
                }

            }

        });

    }

    @Override
    public void stopHandle() {
        started = false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        handleEvent();
    }
}
