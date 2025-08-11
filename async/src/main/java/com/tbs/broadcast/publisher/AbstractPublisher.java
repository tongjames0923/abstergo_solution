package com.tbs.broadcast.publisher;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;
import com.tbs.common.error.AssertUtil;

/**
 * 抽象事件发布者实现类
 * <p>
 * 该类提供了事件发布者的基本实现，包括发布者ID的管理、事件总线的引用以及事件发布的核心逻辑。
 * 具体的发布者实现类可以继承此类，并实现{@link #makePublishResult(IEvent, HandleMode)}方法来创建特定的发布结果对象。
 *
 * @author abstergo
 */
public abstract class AbstractPublisher implements IPublisher {
	/**
	 * 发布者唯一标识
	 */
	private String publisherId;

	/**
	 * 事件总线，用于将事件添加到处理队列中
	 */
	private IEventBus eventBus;

	/**
	 * 构造函数
	 *
	 * @param publisherId 发布者ID，不能为空
	 * @param eventBus    事件总线，用于处理事件发布
	 * @throws IllegalArgumentException 如果publisherId为空时抛出
	 */
	public AbstractPublisher(String publisherId, IEventBus eventBus) {
		AssertUtil.hasText(publisherId, "发布者ID不能为空");
		this.publisherId = publisherId;
		this.eventBus = eventBus;
	}

	@Override
	public String getPushlisherId() {
		return publisherId;
	}

	/**
	 * 创建发布结果
	 * <p>
	 * 由子类实现，用于根据事件和处理模式创建相应的发布结果对象。
	 *
	 * @param event      要发布的事件
	 * @param handleMode 事件处理模式（同步或异步）
	 * @param <T>        发布结果的具体类型
	 * @return 包含事件发布相关信息的发布结果对象
	 */
	protected abstract IPublishResult makePublishResult(IEvent event, HandleMode handleMode);

	/**
	 * 发布事件
	 * <p>
	 * 将事件通过事件总线进行发布，具体流程为： 1.
	 * 调用{@link #makePublishResult(IEvent, HandleMode)}创建发布结果 2. 将发布结果添加到事件总线队列中 3.
	 * 返回发布结果
	 *
	 * @param event      要发布的事件
	 * @param handleMode 事件处理模式（同步或异步）
	 * @return 事件发布结果
	 */
	@Override
	public IPublishResult publish(IEvent event, HandleMode handleMode) {
		IPublishResult publishResult = makePublishResult(event, handleMode);
		eventBus.inqueueEvent(publishResult);
		return publishResult;
	}

}
