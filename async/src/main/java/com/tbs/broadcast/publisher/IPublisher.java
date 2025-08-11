package com.tbs.broadcast.publisher;

import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;

/**
 * 发布者接口
 * <p>
 * 该接口定义了事件发布者的核心功能，包括获取发布者标识、发布事件等操作。 发布者负责将事件发布到事件总线中，支持同步和异步两种发布模式。
 *
 * @author abstergo
 */
public interface IPublisher {
	/**
	 * 获取发布者ID
	 * <p>
	 * 每个发布者都有唯一的标识符，用于区分不同的发布者实例
	 *
	 * @return 发布者唯一标识符
	 */
	String getPushlisherId();

	/**
	 * 发布事件
	 * <p>
	 * 将指定的事件按照指定的处理模式发布到事件总线中
	 *
	 * @param event      要发布的事件对象，不能为null
	 * @param handleMode 事件处理模式，可以是同步(SYNC)或异步(ASYNC)，不能为null
	 * @return 事件发布结果，包含事件处理的相关信息
	 */
	IPublishResult publish(IEvent event, HandleMode handleMode);

	/**
	 * 同步发布事件
	 * <p>
	 * 将指定的事件以同步模式发布到事件总线中。同步模式下，事件会在当前线程中立即处理
	 *
	 * @param event 要发布的事件对象，不能为null
	 * @return 事件发布结果，包含事件处理的相关信息
	 */
	default IPublishResult pushlishSync(IEvent event) {
		return publish(event, com.tbs.broadcast.enums.HandleMode.SYNC);
	}

	/**
	 * 异步发布事件
	 * <p>
	 * 将指定的事件以异步模式发布到事件总线中。异步模式下，事件会被放入队列中等待处理
	 *
	 * @param event 要发布的事件对象，不能为null
	 * @return 事件发布结果，包含事件处理的相关信息
	 */
	default IPublishResult pushlishAsync(IEvent event) {
		return publish(event, HandleMode.ASYNC);
	}

}
