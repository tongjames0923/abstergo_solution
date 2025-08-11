package com.tbs.broadcast.publisher;

import java.util.Collection;

import com.tbs.broadcast.enums.EventStatusEnum;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;
import com.tbs.common.error.AssertUtil;

/**
 * 抽象发布结果实现类
 * <p>
 * 该类提供了事件发布结果的基本实现，包括事件、发布者、处理模式等核心属性的管理，
 * 以及事件状态变更和异常处理的相关逻辑。具体实现类需要继承此类并实现抽象方法。
 *
 * @author tongj
 */
public abstract class AbstractPublishResult implements IPublishResult {

	/**
	 * 获取事件状态监听器集合
	 * <p>
	 * 用于在事件状态变更或处理异常时通知所有监听器
	 *
	 * @return 事件状态监听器集合
	 */
	protected abstract Collection<IEventStatusListenner> getStatusListeners();

	/**
	 * 根据发布者ID获取对应的发布者实例
	 *
	 * @param publisherId 发布者唯一标识
	 * @return 对应的发布者实例
	 */
	protected abstract IPublisher getPublisherById(String publisherId);

	/**
	 * 事件状态，默认为IDLE（空闲状态）
	 */
	private EventStatusEnum eventStatus = EventStatusEnum.IDLE;

	/**
	 * 事件对象
	 */
	private IEvent event;

	/**
	 * 发布者ID
	 */
	private String publisherId;

	/**
	 * 事件处理模式（同步或异步）
	 */
	private HandleMode handleMode;

	/**
	 * 事件处理过程中发生的异常
	 */
	private Throwable throwable;

	/**
	 * 构造函数
	 *
	 * @param event      事件对象，不能为null
	 * @param publisher  发布者ID，不能为空
	 * @param handleMode 处理模式（同步或异步），不能为null
	 * @throws IllegalArgumentException 当event、publisher或handleMode为null或空时抛出
	 */
	public AbstractPublishResult(IEvent event, String publisher, HandleMode handleMode) {
		AssertUtil.notNull(event, "event can not be null");
		AssertUtil.hasText(publisher, "publisher can not be null");
		AssertUtil.notNull(handleMode, "handleMode can not be null");
		this.event = event;
		this.publisherId = publisher;
		this.handleMode = handleMode;
	}

	/**
	 * 默认构造函数
	 */
	public AbstractPublishResult() {
	}

	@Override
	public IEvent getEvent() {
		return event;
	}

	@Override
	public IPublisher getPublisher() {
		return getPublisherById(publisherId);
	}

	@Override
	public HandleMode getHandleMode() {
		return handleMode;
	}

	@Override
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * 设置异常信息，并通知所有事件状态监听器处理出现异常
	 * <p>
	 * 当事件处理过程中发生异常时调用此方法，会将异常信息保存并通知所有监听器
	 *
	 * @param throwable 异常对象
	 */
	@Override
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
		getStatusListeners().forEach(listenner -> listenner.onEventHandleError(throwable, this));
	}

	/**
	 * 判断事件是否已完成（处于终态）
	 * <p>
	 * 终态表示该状态为处理最终状态，后续不可再变更
	 *
	 * @return true表示事件已完成，false表示未完成
	 */
	private boolean isEventComplete() {
		return getEventStatus().isTerminal();
	}

	/**
	 * 设置事件状态，并通知所有事件状态监听器状态已变更
	 * <p>
	 * 只有当事件处于终态时才能设置新的状态，否则会抛出异常
	 *
	 * @param eventStatus 事件状态，不能为null
	 * @throws IllegalArgumentException 当eventStatus为null或事件未处于终态时抛出
	 */
	@Override
	public void setEventStatus(EventStatusEnum eventStatus) {
		AssertUtil.notNull(eventStatus, "eventStatus can not be null");
		AssertUtil.isTrue(isEventComplete(), "can not set event status to " + eventStatus.getValue()
				+ " when event status is " + getEventStatus().getValue());
		getStatusListeners().forEach(listenner -> listenner.onEventStatusChange(eventStatus, getEventStatus(), this));
		this.eventStatus = eventStatus;
	}

	@Override
	public EventStatusEnum getEventStatus() {
		return eventStatus;
	}

}
