package com.tbs.boardcast.dispatcher.impl;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.dispatcher.IEventDispatcher;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.subscriber.AbstractSubscriber;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
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
public abstract class AbstractEventDispatcher implements IEventDispatcher, InitializingBean, DisposableBean {

    @Resource
    private IEventBus eventBus;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractEventDispatcher.class);

    private ExecutorService executorService;
    private boolean started = false;

    private Map<String, AbstractSubscriber> subscriberMap = new HashMap<>();

    /**
     * 构造函数
     *
     * @param executorService
     */
    public AbstractEventDispatcher(ExecutorService executorService) {
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
            logger.trace("开始处理事件");
            while (started) {
                IPublishResult<?> iPublishResult = null;
                try {
                    iPublishResult = getEventBus().nextUnhandledEvent(Duration.ofMinutes(1));
                    if (iPublishResult == null) {
                        logger.trace("空事件超时");
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
                } catch (Throwable e) {
                    handleError(e, iPublishResult);
                }
            }
            logger.trace("停止处理事件");
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

    @Override
    public void destroy() throws Exception {
        stopHandle();
    }
}
