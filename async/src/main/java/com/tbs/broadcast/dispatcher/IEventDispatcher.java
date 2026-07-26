package com.tbs.broadcast.dispatcher;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.subscriber.AbstractSubscriber;

/**
 * 事件分发器接口
 * <p>
 * 该接口定义了事件分发器的核心功能，包括订阅者的注册与注销、事件总线的管理、
 * 事件处理异常的处理以及事件处理流程的控制等操作。事件分发器负责协调事件的整个处理流程。
 *
 * @author abstergo
 */
public interface IEventDispatcher {

	/**
	 * 注册订阅者
	 * <p>
	 * 将指定的订阅者注册到分发器中，使其能够接收并处理符合条件的事件。 注册后的订阅者会在事件发布时收到相应的事件通知。
	 *
	 * @param subscriber 要注册的订阅者实例，不能为null
	 */
	void register( AbstractSubscriber subscriber);

	/**
	 * 注销订阅者
	 * <p>
	 * 从分发器中移除指定的订阅者，使其不再接收事件通知。 注销后的订阅者将不会收到任何事件通知。
	 *
	 * @param subscriber 要注销的订阅者实例，不能为null
	 */
	void unregister(AbstractSubscriber subscriber);

	/**
	 * 获取事件总线
	 * <p>
	 * 返回与该分发器关联的事件总线实例，用于事件的排队和分发管理。
	 *
	 * @return 事件总线实例
	 */
	IEventBus getEventBus();

	/**
	 * 处理事件处理异常
	 * <p>
	 * 当事件在处理过程中发生异常时调用此方法进行统一异常处理， 可以记录日志、触发告警或执行其他异常处理逻辑。
	 *
	 * @param throwable     异常对象，包含异常的详细信息
	 * @param publishResult 发布结果对象，包含事件和处理上下文信息
	 * @param <T>           泛型参数
	 */
	<T> void handleError(Throwable throwable, IPublishResult publishResult);

	/**
	 * 处理事件
	 * <p>
	 * 根据事件处理结果，将事件分发给相应的订阅者进行处理。
	 *
	 * @param result 事件处理结果对象，包含事件和订阅者信息
	 */
	void handleEvent(IPublishResult result);

	/**
	 * 获取分布式事件支持
	 * <p>
	 * 返回该分发器所支持的分布式事件处理支持实例，用于处理分布式事件。
	 *
	 * @return 分布式事件处理支持实例
	 */
	Optional<IDistributedDispatcherSupport> getDistributedDispatcherSupport();

}
