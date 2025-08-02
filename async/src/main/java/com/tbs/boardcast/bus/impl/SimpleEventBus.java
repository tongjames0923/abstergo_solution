package com.tbs.boardcast.bus.impl;

import com.tbs.boardcast.bus.AbstractEventBus;
import com.tbs.boardcast.bus.AbstractSemaphoreEventBus;
import com.tbs.boardcast.bus.IEventBus;
import com.tbs.boardcast.enums.EventStatusEnum;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.error.AssertUtil;
import com.tbs.lock.ILock;
import com.tbs.lock.util.LockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * 默认事件总线
 *
 * @author abstergo
 */
public class SimpleEventBus extends AbstractSemaphoreEventBus<IPublishResult> {

    private static PriorityQueue<IPublishResult> queue = new PriorityQueue<>(new Comparator<IPublishResult>() {
        @Override
        public int compare(IPublishResult o1, IPublishResult o2) {
            return o1.getEvent().getEventPriority() - o2.getEvent().getEventPriority();
        }
    });

    @Override
    protected Queue<IPublishResult> getQueue() {
        return queue;
    }

    @Override
    protected IPublishResult put(IPublishResult r) {
        return r;
    }
}
