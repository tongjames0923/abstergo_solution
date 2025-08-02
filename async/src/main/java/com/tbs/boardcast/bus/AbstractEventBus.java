package com.tbs.boardcast.bus;

import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.lock.IConditionLock;
import com.tbs.lock.ILock;
import com.tbs.lock.util.LockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @author tongj
 */
public abstract class AbstractEventBus<R extends IPublishResult> implements IEventBus {
    /**
     * 获取队列
     *
     * @return
     */
    protected abstract Queue<R> getQueue();

    /**
     * 将传入的事件转化为支持的事件
     *
     * @param r
     * @param <T>
     * @return
     */
    protected abstract <T> R put(IPublishResult<T> r);

    private static Logger logger = LoggerFactory.getLogger(AbstractEventBus.class);

    ILock lock = null;

    @PostConstruct
    public void init() {
        logger.debug("初始化事件总线");
        lock = LockUtil.getInstance().getLockerByClass(this.getClass());
    }

    /**
     * 队列信号
     */
    protected abstract void signalInQueue();

    /**
     * 队列条件
     *
     * @param timeout
     * @param unit
     * @return
     */
    protected abstract boolean awaitQueue(long timeout, TimeUnit unit) throws InterruptedException;

    @Override
    public <T> void inqueueEvent(IPublishResult<T> publishResult) {
        preInqueueCheck(publishResult);
        LockUtil.getInstance().lockThen(lock, () -> {
            logger.debug("添加事件到队列:{}", publishResult.getEvent().getEventName());
            getQueue().add(put(publishResult));
            signalInQueue();
        });
    }

    protected static <T> void preInqueueCheck(IPublishResult<T> publishResult) {
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
    }

    @Override
    public IPublishResult<?> nextUnhandledEvent(Duration duration) {
        IPublishResult<?>[] result = new IPublishResult[1];
        LockUtil.getInstance().lockThen(lock, () -> {

            if (getQueue().isEmpty()) {
                logger.debug("等待事件");
                boolean r = false;
                try {
                    r = awaitQueue(duration.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.error("条件等待异常", e);
                    Thread.currentThread().interrupt();
                }
                logger.debug("等待事件结束，是否超时：{}", !r);
            }
            if (!getQueue().isEmpty()) {
                result[0] = getQueue().poll();
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
        return getQueue().size();
    }
}
