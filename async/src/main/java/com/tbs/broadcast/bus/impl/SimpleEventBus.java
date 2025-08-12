package com.tbs.broadcast.bus.impl;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.annotation.Resource;

import com.tbs.broadcast.bus.AbstractEventBus;
import com.tbs.broadcast.bus.AbstractSemaphoreEventBus;
import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.publisher.IPublishResult;

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

	@Resource
	private IDistributedBusSupport distributedBusSupport;

	/**
	 * 事件优先级队列
	 * <p>
	 * 使用PriorityQueue实现，根据事件优先级进行排序，优先级数值越大的事件越优先被处理。
	 * 队列中的元素类型为IPublishResult，表示事件发布结果。 该队列是静态的，在所有SimpleEventBus实例间共享。
	 *
	 * @see IPublishResult 事件发布结果接口
	 * @see Comparator 比较器接口
	 */
	private static PriorityQueue<IPublishResult> queue = new PriorityQueue<>(new Comparator<IPublishResult>() {
		@Override
		public int compare(IPublishResult o1, IPublishResult o2) {
			return o1.getEvent().getEventPriority() - o2.getEvent().getEventPriority();
		}
	});

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
		return Optional.ofNullable(distributedBusSupport);
	}
}
