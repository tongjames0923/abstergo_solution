package com.tbs.broadcast.publisher;

import com.tbs.broadcast.enums.EventStatusEnum;

/**
 * 事件状态监听器接口
 * <p>
 * 该接口定义了事件状态变更和事件处理异常的监听方法。实现该接口的类可以监听 事件发布结果的状态变化，并在状态变更或处理异常时执行相应的操作。
 *
 * @param <T> 监听器处理的数据类型
 * @author abstergo
 */
public interface IEventStatusListenner<T> {
	/**
	 * 事件状态改变回调方法
	 * <p>
	 * 当事件的处理状态发生变更时调用此方法，可以用于记录状态变更日志、 触发相关业务逻辑等操作。
	 *
	 * @param eventStatus    新的事件状态，不能为null
	 * @param oldEventStatus 原始事件状态，不能为null
	 * @param publishResult  对应的发布结果对象，包含事件的详细信息
	 */
	void onEventStatusChange(EventStatusEnum eventStatus, EventStatusEnum oldEventStatus, IPublishResult publishResult);

	/**
	 * 事件处理异常回调方法
	 * <p>
	 * 当事件处理过程中发生异常时调用此方法，可以用于记录异常日志、 发送告警通知、执行异常处理逻辑等操作。
	 *
	 * @param throwable     异常对象，包含异常的详细信息
	 * @param publishResult 对应的发布结果对象，包含事件的详细信息
	 */
	void onEventHandleError(Throwable throwable, IPublishResult publishResult);
}
