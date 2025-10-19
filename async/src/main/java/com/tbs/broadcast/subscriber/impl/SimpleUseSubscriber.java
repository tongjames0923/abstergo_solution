package com.tbs.broadcast.subscriber.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;

import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.broadcast.subscriber.AbstractSubscriber;
import com.tbs.broadcast.subscriber.IReceiveHandler;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;

/**
 * 简易使用订阅者
 * <p>
 * 这是一个具体的订阅者实现，可以通过Builder模式快速创建一个订阅者实例。
 * 该订阅者支持根据事件ID和事件名称进行过滤，并通过IReceiveHandler处理接收到的事件。
 * 实现了IStartup接口，可以在系统启动时自动注册到事件分发器中。
 *
 * @author tongj
 */
public class SimpleUseSubscriber extends AbstractSubscriber implements IStartup {
	/**
	 * 订阅者ID，用于唯一标识一个订阅者
	 */
	private String subscriberId;

	/**
	 * 是否支持任意事件ID 如果为true，则不进行事件ID过滤
	 */
	private boolean supportAnyEventId;

	/**
	 * 是否支持任意事件名称 如果为true，则不进行事件名称过滤
	 */
	private boolean supportAnyEventName;

	/**
	 * 支持的事件ID集合 只有事件ID在此集合中的事件才会被处理
	 */
	private Set<String> supportEventIds;

	/**
	 * 支持的事件名称集合 只有事件名称在此集合中的事件才会被处理
	 */
	private Set<String> supportEventNames;

	/**
	 * 事件处理回调函数
	 */
	private IReceiveHandler eventHandler;

	/**
	 * 订阅者构建器
	 * <p>
	 * 通过Builder模式创建SimpleUseSubscriber实例，可以灵活配置订阅者的行为
	 */
	public static class Builder {
		/**
		 * 订阅者ID
		 */
		private String subscriberId;

		/**
		 * 是否支持任意事件ID，默认为true
		 */
		private boolean supportAnyEventId = true;

		/**
		 * 是否支持任意事件名称，默认为true
		 */
		private boolean supportAnyEventName = true;

		/**
		 * 支持的事件ID集合
		 */
		private Set<String> supportEventIds;

		/**
		 * 支持的事件名称集合
		 */
		private Set<String> supportEventNames;

		/**
		 * 事件处理回调函数
		 */
		private IReceiveHandler eventHandler;

		/**
		 * 创建一个订阅者构建器
		 *
		 * @param subscriberId 订阅者ID，用于唯一标识一个订阅者，不能为空
		 * @param eventHandler 事件处理函数，用于处理接收到的事件，不能为空
		 * @return Builder实例
		 * @throws IllegalArgumentException 如果eventHandler为null或subscriberId为空时抛出
		 */
		public static Builder newBuilder(String subscriberId, @NotNull IReceiveHandler eventHandler) {
			AssertUtil.notNull(eventHandler, "eventHandler can not be null");
			AssertUtil.isTrue(StrUtil.isEmpty(subscriberId), "subscriberId can not be empty");
			Builder builder = new Builder();
			builder.subscriberId = subscriberId;
			builder.eventHandler = eventHandler;
			return builder;
		}

		/**
		 * 设置支持的事件ID列表
		 * <p>
		 * 调用此方法后，订阅者只会处理事件ID在指定列表中的事件。 如果传入的eventIds为空或null，则不会改变默认行为（支持任意事件ID）。
		 *
		 * @param eventIds 支持的事件ID集合
		 * @return Builder实例，用于链式调用
		 */
		public Builder supportEventIds(@NotNull Collection<String> eventIds) {
			if (CollUtil.isNotEmpty(eventIds)) {
				this.supportAnyEventId = false;
				this.supportEventIds = eventIds.stream().collect(Collectors.toSet());
			}

			return this;
		}

		/**
		 * 设置支持的事件名称列表
		 * <p>
		 * 调用此方法后，订阅者只会处理事件名称在指定列表中的事件。 如果传入的eventNames为空或null，则不会改变默认行为（支持任意事件名称）。
		 *
		 * @param eventNames 支持的事件名称集合
		 * @return Builder实例，用于链式调用
		 */
		public Builder supportEventNames(@NotNull Collection<String> eventNames) {
			if (CollUtil.isNotEmpty(eventNames)) {
				this.supportAnyEventName = false;
				this.supportEventNames = eventNames.stream().collect(Collectors.toSet());
			}
			return this;
		}

		/**
		 * 构建SimpleUseSubscriber实例
		 *
		 * @return 构建完成的SimpleUseSubscriber实例
		 */
		public SimpleUseSubscriber build() {
			SimpleUseSubscriber subscriber = new SimpleUseSubscriber(eventHandler, supportEventNames, supportEventIds,
					supportAnyEventName, supportAnyEventId, subscriberId);
			return subscriber;
		}
	}

	/**
	 * 构造函数
	 *
	 * @param eventHandler        事件处理回调函数，不能为空
	 * @param supportEventNames   支持的事件名称集合，可以为null
	 * @param supportEventIds     支持的事件ID集合，可以为null
	 * @param supportAnyEventName 是否支持任意事件名称
	 * @param supportAnyEventId   是否支持任意事件ID
	 * @param subscriberId        订阅者ID，不能为空
	 */
	protected SimpleUseSubscriber(@NotNull IReceiveHandler eventHandler, Set<String> supportEventNames,
			Set<String> supportEventIds, boolean supportAnyEventName, boolean supportAnyEventId,
			@NotNull String subscriberId) {
		this.eventHandler = eventHandler;
		this.subscriberId = subscriberId;

		this.supportEventNames = supportEventNames == null ? new HashSet<>(1) : supportEventNames;
		this.supportEventIds = supportEventIds == null ? new HashSet<>(1) : supportEventIds;
		this.supportAnyEventName = supportAnyEventName;
		this.supportAnyEventId = supportAnyEventId;

	}

	@Resource
	private IEventDispatcher dispatcher;

	@Override
	public String getSubscriberId() {
		return subscriberId;
	}

	/**
	 * 判断是否支持指定的事件
	 * <p>
	 * 根据事件ID和事件名称判断当前订阅者是否支持该事件。 只有当事件ID和事件名称都满足条件时才返回true。
	 *
	 * @param eventId   事件ID
	 * @param eventName 事件名称
	 * @return 如果支持该事件返回true，否则返回false
	 */
	@Override
	public boolean supportEvent(String eventId, String eventName) {
		return (supportAnyEventId || supportEventIds.contains(eventId))
				&& (supportAnyEventName || supportEventNames.contains(eventName));
	}

	/**
	 * 判断是否支持指定的发布者
	 * <p>
	 * 默认实现为支持所有发布者
	 *
	 * @param publisher 发布者实例
	 * @return 总是返回true
	 */
	@Override
	public boolean supportPublisher(IPublisher publisher) {
		return true;
	}

	/**
	 * 处理接收到的事件
	 * <p>
	 * 将事件处理委托给eventHandler回调函数
	 *
	 * @param event     接收到的事件
	 * @param publisher 事件发布者
	 * @return 处理结果，参考{@link AbstractSubscriber}中的常量定义:
	 *         {@link AbstractSubscriber#RECEIVE_SUCCESS}表示处理成功,
	 *         {@link AbstractSubscriber#RECEIVE_FAILED}表示处理失败,
	 *         {@link AbstractSubscriber#RECEIVE_RETRY}表示需要重试
	 */
	@Override
	protected int onReceive(IEvent event, IPublisher publisher) {
		return eventHandler.recive(event, publisher);
	}

	/**
	 * 启动方法，将当前订阅者注册到事件分发器中
	 *
	 * @throws RuntimeException 如果注册过程中发生错误
	 */
	@Override
	public void startUp() throws RuntimeException {
		dispatcher.register(this);
	}
}
