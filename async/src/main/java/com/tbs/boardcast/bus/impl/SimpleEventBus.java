package com.tbs.boardcast.bus.impl;

import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.AssertUtil;
import com.tbs.lock.ILock;
import com.tbs.lock.util.LockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * 默认事件总线
 *
 * @author abstergo
 */
public class SimpleEventBus implements IEventBus {

    private static Logger logger = LoggerFactory.getLogger(SimpleEventBus.class);

    private PriorityQueue<IPublishResult<?>> eventQueue = new PriorityQueue<>(new Comparator<IPublishResult<?>>() {
        @Override
        public int compare(IPublishResult<?> o1, IPublishResult<?> o2) {
            return o1.getEvent().getEventPriority() - o2.getEvent().getEventPriority();
        }
    });

    ILock lock = null;

    Condition condition;

    @PostConstruct
    public void init() {
        logger.debug("初始化事件总线");
        lock = LockUtil.getInstance().getLockerByClass(this.getClass());
        condition = lock.newCondition();
    }

    @Override
    public <T> void inqueueEvent(IPublishResult<T> publishResult) {
        AssertUtil.notNull(publishResult, "publishResult can not be null");
        AssertUtil.notNull(publishResult.getEvent(), "event can not be null");
        if (publishResult.getEventStatus() == null ||
            publishResult.getEventStatus() == EventStatusEnum.IDLE ||
            publishResult.getEventStatus() == EventStatusEnum.REQUIRE_RETRY) {
            publishResult.setEventStatus(EventStatusEnum.IN_QUEUE);
        } else {
            AssertUtil.throwError(
                () -> new FormattedRuntimeException("事件状态错误,入队的状态仅允许IDLE，REQUIRE_RETRY,IN_QUEUE"));
        }
        LockUtil.getInstance().lockThen(lock, () -> {
            logger.debug("添加事件到队列:{}", publishResult.getEvent().getEventName());
            eventQueue.add(publishResult);
            condition.signal();
        });
    }

    @Override
    public IPublishResult<?> nextUnhandledEvent(Duration duration) {
        IPublishResult<?>[] result = new IPublishResult[1];
        LockUtil.getInstance().lockThen(lock, () -> {

            if (eventQueue.isEmpty()) {
                try {
                    logger.debug("等待事件");
                    boolean r = condition.await(duration.toMillis(), TimeUnit.MILLISECONDS);
                    logger.debug("等待事件结束，是否超时：{}", !r);
                } catch (InterruptedException e) {
                    logger.error("条件等待异常", e);
                    Thread.currentThread().interrupt(); // 恢复中断状态
                }
            }
            if (!eventQueue.isEmpty()) {
                result[0] = eventQueue.poll();
                logger.debug("获取事件:{}", result[0].getEvent().getEventId());
            }
        });

        if (result[0] != null) {
            result[0].setEventStatus(EventStatusEnum.IN_PROCESS);
        }
        return result[0];
    }

    @Override
    public int getEventQueueSize() {
        return eventQueue.size();
    }
}
