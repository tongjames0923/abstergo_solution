package com.tbs.broadcast.dispatcher.impl;

import com.tbs.broadcast.dispatcher.AbstractEventDispatcher;
import com.tbs.broadcast.dispatcher.IDistributedDispatcherSupport;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.common.util.framework.SpringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 简单事件分发器实现类
 * <p>
 * 该类继承自{@link AbstractEventDispatcher}，提供了基本的事件分发功能。
 * 它负责将事件发布结果分发给所有支持该事件的订阅者，并根据事件的处理模式（同步或异步）来决定如何处理事件。 同时，该类还提供了错误处理机制和分布式分发支持。
 *
 * @see AbstractEventDispatcher
 * @see IPublishResult
 * @see HandleMode
 */
@Slf4j
public class SimpleEventDispatcher extends AbstractEventDispatcher {

	/**
	 * 事件错误处理接口
	 * <p>
	 * 用于定义当事件处理过程中发生错误时的处理逻辑。
	 *
	 * @see IPublishResult
	 */
	public static interface IHandleEventError {
		/**
		 * 处理事件错误
		 *
		 * @param throwable     异常信息
		 * @param publishResult 事件发布结果，包含事件和发布相关信息
		 * @see IPublishResult
		 */
		void handleError(Throwable throwable, IPublishResult publishResult);
	}

	/**
	 * 事件错误处理器实例
	 * <p>
	 * 用于处理事件分发过程中发生的错误
	 *
	 * @see #setHandleEventError(IHandleEventError)
	 * @see #getHandleEventError()
	 */
	@Getter
	@Setter
	private IHandleEventError handleEventError;

	private IDistributedDispatcherSupport distributedDispatcherSupport;

	/**
	 * 处理事件分发过程中发生的错误
	 * <p>
	 * 当事件处理过程中发生异常时，调用此方法进行错误处理。 如果未设置错误处理器({@link #handleEventError})，则仅记录错误日志。
	 *
	 * @param <T>           类型参数
	 * @param throwable     异常信息
	 * @param publishResult 事件发布结果，可能为null
	 * @see IHandleEventError#handleError(Throwable, IPublishResult)
	 */
	@Override
	public <T> void handleError(Throwable throwable, IPublishResult publishResult) {
		if (getHandleEventError() == null) {
			log.error("handleEventError is null", throwable);
			return;
		}
		getHandleEventError().handleError(throwable, publishResult);
	}

	/**
	 * 处理事件分发
	 * <p>
	 * 遍历所有已注册的订阅者，对于支持当前事件和发布者的订阅者，
	 * 根据事件的处理模式({@link IPublishResult#getHandleMode()})来决定是同步还是异步处理事件。
	 *
	 * @param result 事件发布结果，包含事件、发布者和处理模式等信息
	 * @see HandleMode
	 * @see com.tbs.broadcast.subscriber.AbstractSubscriber#recive(IPublishResult)
	 * @see com.tbs.broadcast.subscriber.AbstractSubscriber#reciveAsync(IPublishResult,
	 *      IPublisher)
	 */
	@Override
	public void handleEvent(IPublishResult result) {
		getSubscribers().stream().forEach(subscriber -> {
			if (subscriber.supportEvent(result.getEvent().getEventId(), result.getEvent().getEventName())
					&& subscriber.supportPublisher(result.getPublisher())) {
				if (result.getHandleMode() == HandleMode.SYNC) {
					subscriber.recive(result);
				} else {
					subscriber.reciveAsync(result, result.getPublisher());
				}
			}
		});
	}

	private Optional<IDistributedDispatcherSupport> dispatcherSupport;

	/**
	 * 获取分布式分发支持实例
	 * <p>
	 * 返回用于支持分布式环境下事件分发的实例。
	 *
	 * @return 分布式分发支持实例的Optional包装，如果未设置则返回空Optional
	 * @see IDistributedDispatcherSupport
	 */
	@Override
	public Optional<IDistributedDispatcherSupport> getDistributedDispatcherSupport() {
		if (distributedDispatcherSupport == null) {
			synchronized (this) {
				if (distributedDispatcherSupport == null) {
					dispatcherSupport = SpringUtil.getBeanOptional(IDistributedDispatcherSupport.class);
				}
			}
		}
		return dispatcherSupport;
	}
}
