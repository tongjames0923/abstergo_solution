package com.tbs.boardcast.bus;

import com.tbs.boardcast.publisher.IPublishResult;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 带信号量的抽象事件总线
 *
 * @param <R>
 * @author tongj
 */
public abstract class AbstractSemaphoreEventBus<R> extends AbstractEventBus<R> {

    Semaphore semaphore = new Semaphore(0);

    @Override
    protected void signalInQueue() {
        semaphore.release();
    }

    @Override
    protected boolean awaitQueue(long timeout, TimeUnit unit) throws InterruptedException {
        return semaphore.tryAcquire(timeout, unit);
    }
}
