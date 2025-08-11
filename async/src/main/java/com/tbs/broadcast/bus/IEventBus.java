package com.tbs.broadcast.bus;

import java.time.Duration;

import com.tbs.broadcast.publisher.IPublishResult;

/**
 * 事件总线接口
 * <p>
 * 该接口定义了事件总线的核心功能，负责事件的排队、分发和管理。 事件总线作为事件发布者和事件订阅者之间的中介，实现了事件的解耦传递。
 * 通过事件队列机制，支持事件的异步处理和优先级排序等功能。
 *
 * @author abstergo
 * @see IPublishResult 事件发布结果接口
 */
public interface IEventBus {

	/**
	 * 添加事件到队列
	 * <p>
	 * 将指定的事件发布结果对象添加到事件处理队列中，等待后续处理。 该方法通常由事件发布者调用，将事件提交到事件总线进行处理。
	 *
	 * @param publishResult 事件发布结果对象，包含了待处理的事件及其相关信息，不能为null
	 * @see IPublishResult 事件发布结果接口
	 */
	void inqueueEvent(IPublishResult publishResult);

	/**
	 * 获取下一个未处理的事件
	 * <p>
	 * 从事件队列中获取下一个待处理的事件发布结果对象。如果队列为空， 方法将阻塞等待直到有事件可用或达到指定的超时时间。
	 *
	 * @param timeOut 超时时间，如果在指定时间内没有获取到事件，则返回null
	 * @return 事件发布结果对象，如果超时则返回null
	 * @see IPublishResult 事件发布结果接口
	 */
	IPublishResult nextUnhandledEvent(Duration timeOut);

	/**
	 * 获取事件队列大小
	 * <p>
	 * 返回当前事件队列中等待处理的事件数量，用于监控和管理事件处理负载。
	 *
	 * @return 事件队列中的事件数量
	 */
	int getEventQueueSize();

}
