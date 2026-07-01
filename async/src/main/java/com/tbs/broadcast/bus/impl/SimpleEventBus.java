package com.tbs.broadcast.bus.impl;

import com.tbs.broadcast.bus.AbstractEventBus;
import com.tbs.broadcast.bus.AbstractSemaphoreEventBus;
import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.util.framework.SpringUtil;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 简单事件总线实现类
 * <p>
 * 该类是AbstractSemaphoreEventBus的具体实现，提供了基于优先级队列的事件总线功能。
 * 使用PriorityQueue作为事件队列，根据事件的优先级对事件进行排序，优先级高的事件会被优先处理。
 * 继承自AbstractSemaphoreEventBus，使用信号量机制实现线程间的同步。
 *
 * @author abstergo
 * @see AbstractSemaphoreEventBus 基于信号量的抽象事件总线
 * @see IPublishResult 事件发布结果接口
 * @see PriorityQueue 优先级队列实现
 */
public class SimpleEventBus extends AbstractSemaphoreEventBus {

	private Optional<IDistributedBusSupport> distributedBusSupport;

	/**
	 * 事件优先级队列
	 * <p>
	 * 使用PriorityQueue实现，根据事件优先级进行排序，优先级数值越大的事件越优先被处理。
	 * 队列中的元素类型为IPublishResult，表示事件发布结果。依赖Spring单例保证只有一个实例。
	 *
	 * @see IPublishResult 事件发布结果接口
	 * @see Comparator 比较器接口
	 */
	private final PriorityQueue<IPublishResult> queue = new PriorityQueue<>(Comparator.comparingInt(
			o -> o.getEvent().getEventPriority()));

	/**
	 * 获取事件队列
	 * <p>
	 * 实现AbstractEventBus抽象方法，返回用于存储事件的优先级队列实例。
	 * 所有继承自AbstractEventBus的方法都会通过此方法访问事件队列。
	 *
	 * @return 事件优先级队列实例
	 * @see AbstractEventBus#getQueue() 抽象事件总线的获取队列方法
	 * @see Queue 队列接口
	 * @see PriorityQueue 优先级队列实现
	 */
	@Override
	protected Queue<IPublishResult> getQueue() {
		return queue;
	}

	@Override
	public Optional<IDistributedBusSupport> getDistributedBusSupport() {
		if (distributedBusSupport == null) {
			synchronized (this) {
				if (distributedBusSupport == null) {
					distributedBusSupport = SpringUtil.getBeanOptional(IDistributedBusSupport.class);
				}
			}
		}
		return distributedBusSupport;
	}
}
