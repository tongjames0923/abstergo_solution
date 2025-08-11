package com.tbs.broadcast.publisher.impl;

import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.AbstractPublisher;
import com.tbs.broadcast.publisher.IPublishResult;

import lombok.Getter;

/**
 * 通过外部导入分发器的发布者实现类
 * <p>
 * 该类是一个具体的发布者实现，它通过外部传入的事件分发器来完成事件发布功能。 与普通发布者不同的是，该发布者在构造时需要传入一个事件分发器，
 * 并通过分发器获取事件总线来进行事件发布。
 *
 * @author abstergo
 */
public class DispatcherImportedPublisher extends AbstractPublisher {

	/**
	 * 事件分发器，用于处理事件的分发逻辑
	 */
	@Getter
	private IEventDispatcher eventDispatcher;

	/**
	 * 构造函数
	 *
	 * @param publisherId     发布者ID，不能为空
	 * @param eventDispatcher 事件分发器，不能为null
	 */
	public DispatcherImportedPublisher(String publisherId, IEventDispatcher eventDispatcher) {
		super(publisherId, eventDispatcher.getEventBus());
		this.eventDispatcher = eventDispatcher;
	}

	/**
	 * 创建发布结果
	 * <p>
	 * 根据传入的事件和处理模式创建Spring管理的可监听发布结果对象
	 *
	 * @param event      要发布的事件
	 * @param handleMode 事件处理模式（同步或异步）
	 * @return Spring管理的可监听发布结果对象
	 */
	@Override
	protected IPublishResult makePublishResult(IEvent event, HandleMode handleMode) {
		return new SpringManagedListenablePublishResult(event, this.getPushlisherId(), handleMode);
	}
}
