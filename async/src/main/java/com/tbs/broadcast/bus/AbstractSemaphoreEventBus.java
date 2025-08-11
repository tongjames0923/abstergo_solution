package com.tbs.broadcast.bus;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.tbs.broadcast.publisher.IPublishResult;

/**
 * 基于信号量的抽象事件总线实现类
 * <p>
 * 该类是AbstractEventBus的抽象实现，使用信号量(Semaphore)机制来管理事件队列的同步访问。
 * 当事件被添加到队列时，通过释放信号量来通知等待的线程有新事件可用； 当需要获取事件时，通过尝试获取信号量来等待新事件的到来。
 * 这种机制可以有效减少线程的忙等待，提高系统性能。
 *
 * @param <R> 泛型参数，表示事件总线处理的事件类型
 * @author tongj
 * @see AbstractEventBus 抽象事件总线基类
 * @see Semaphore 信号量类
 */
public abstract class AbstractSemaphoreEventBus extends AbstractEventBus {

	/**
	 * 信号量对象，用于控制对事件队列的访问
	 * <p>
	 * 初始许可数为0，当有事件入队时会释放一个许可，当需要获取事件时会尝试获取一个许可。 如果没有可用许可，获取操作将会阻塞直到有新的事件入队并释放许可。
	 */
	Semaphore semaphore = new Semaphore(0);

	/**
	 * 当事件入队时发送信号
	 * <p>
	 * 释放一个信号量许可，通知等待事件的线程有新事件可用。 该方法在事件成功添加到队列后被调用。
	 *
	 * @see AbstractEventBus#inqueueEvent(IPublishResult) 事件入队方法
	 */
	@Override
	protected void signalInQueue() {
		semaphore.release();
	}

	/**
	 * 等待队列中有事件可用
	 * <p>
	 * 尝试在指定的超时时间内获取一个信号量许可。如果有许可可用则立即返回true，
	 * 如果在超时时间内没有许可可用则返回false。此方法用于实现事件获取的阻塞等待机制。
	 *
	 * @param timeout 等待超时时间
	 * @param unit    超时时间单位
	 * @return 如果在超时时间内获取到许可返回true，否则返回false
	 * @throws InterruptedException 如果等待过程中线程被中断则抛出该异常
	 * @see AbstractEventBus#nextUnhandledEvent(Duration) 获取未处理事件方法
	 */
	@Override
	protected boolean awaitQueue(long timeout, TimeUnit unit) throws InterruptedException {
		return semaphore.tryAcquire(timeout, unit);
	}
}
