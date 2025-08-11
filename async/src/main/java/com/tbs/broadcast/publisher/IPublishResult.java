package com.tbs.broadcast.publisher;

import com.tbs.broadcast.enums.EventStatusEnum;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;

/**
 * 发布结果接口
 * <p>
 * 该接口定义了事件发布结果的相关操作，包括获取事件信息、发布者信息、处理模式、 异常信息以及事件状态的管理等功能。每个发布结果都与一个具体的事件相关联，
 * 并记录了该事件的处理状态和相关信息。
 *
 * @author abstergo
 */
public interface IPublishResult {
	/**
	 * 获取事件对象
	 * <p>
	 * 返回与该发布结果相关联的事件对象，包含事件的具体数据和信息
	 *
	 * @return 事件对象
	 */
	IEvent getEvent();

	/**
	 * 获取事件发布者
	 * <p>
	 * 返回发布该事件的发布者对象
	 *
	 * @return 事件发布者
	 */
	IPublisher getPublisher();

	/**
	 * 获取事件处理模式
	 * <p>
	 * 返回该事件的处理模式，可以是同步(SYNC)或异步(ASYNC)
	 *
	 * @return 事件处理模式
	 */
	HandleMode getHandleMode();

	/**
	 * 获取事件处理过程中发生的异常
	 * <p>
	 * 如果事件处理过程中没有发生异常，则返回null
	 *
	 * @return 异常对象，如果没有异常则返回null
	 */
	Throwable getThrowable();

	/**
	 * 设置事件处理过程中发生的异常
	 * <p>
	 * 当事件处理过程中发生异常时，通过此方法记录异常信息
	 *
	 * @param throwable 异常对象，可以为null
	 */
	void setThrowable(Throwable throwable);

	/**
	 * 设置事件状态
	 * <p>
	 * 更新事件的处理状态，如处理中、处理完成、处理失败等
	 *
	 * @param eventStatus 事件状态，不能为null
	 */
	void setEventStatus(EventStatusEnum eventStatus);

	/**
	 * 获取事件当前状态
	 * <p>
	 * 返回事件的当前处理状态，默认状态为IDLE(空闲状态)
	 *
	 * @return 事件状态
	 */
	EventStatusEnum getEventStatus();

}
