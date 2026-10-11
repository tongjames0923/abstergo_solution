package com.tbs.broadcast.subscriber;

import jakarta.annotation.Resource;

import com.tbs.broadcast.enums.EventStatusEnum;
import com.tbs.broadcast.event.IEvent;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.thread.IThreadPoolManager;
import com.tbs.thread.impl.SimpleThreadRequest;

/**
 * 抽象订阅者类
 * <p>
 * 定义了订阅者的基本行为和框架，具体订阅者应继承此类并实现相关抽象方法。 提供了同步和异步事件处理机制，以及事件处理结果的处理逻辑。
 *
 * @author abstergo
 */
public abstract class AbstractSubscriber {

	/**
	 * 线程池管理器，用于异步处理事件
	 */
	@Resource
	IThreadPoolManager threadPoolManager;

	/**
	 * 事件处理成功状态码
	 */
	public static final int RECEIVE_SUCCESS = 1;

	/**
	 * 事件处理失败状态码
	 */
	public static final int RECEIVE_FAILED = 0;

	/**
	 * 事件处理需要重试状态码
	 */
	public static final int RECEIVE_RETRY = -1;

	/**
	 * 获取订阅者ID
	 * <p>
	 * 每个订阅者都需要有一个唯一的ID用于标识
	 *
	 * @return 订阅者ID字符串
	 */
	public abstract String getSubscriberId();

	/**
	 * 判断是否支持指定的事件
	 * <p>
	 * 根据事件ID和事件名称判断当前订阅者是否对该事件感兴趣
	 *
	 * @param eventId   事件ID
	 * @param eventName 事件名称
	 * @return 如果支持该事件返回true，否则返回false
	 */
	public abstract boolean supportEvent(String eventId, String eventName);

	/**
	 * 判断是否支持指定的发布者
	 * <p>
	 * 可以根据发布者的类型或ID来判断是否接收其发布的事件
	 *
	 * @param publisher 事件发布者
	 * @return 如果支持该发布者返回true，否则返回false
	 */
	public abstract boolean supportPublisher(IPublisher publisher);

	/**
	 * 处理接收到的事件
	 * <p>
	 * 子类需要实现此方法来处理具体的事件逻辑
	 *
	 * @param event     接收到的事件对象
	 * @param publisher 事件发布者
	 * @return 处理结果状态码: {@link #RECEIVE_SUCCESS}表示处理成功,
	 *         {@link #RECEIVE_FAILED}表示处理失败, {@link #RECEIVE_RETRY}表示需要重试
	 */
	protected abstract int onReceive(IEvent event, IPublisher publisher);

	/**
	 * 接收并处理事件结果
	 * <p>
	 * 根据onReceive方法的返回值设置事件的最终状态
	 *
	 * @param event 事件发布结果对象，包含事件和发布者信息
	 */
	public void recive(IPublishResult event) {
		try {
			int result = onReceive(event.getEvent(), event.getPublisher());
			switch (result) {
			case RECEIVE_FAILED:
				event.setEventStatus(EventStatusEnum.FAILED);
				break;
			case RECEIVE_RETRY:
				event.setEventStatus(EventStatusEnum.REQUIRE_RETRY);
				break;
			case RECEIVE_SUCCESS:
				event.setEventStatus(EventStatusEnum.DONE);
				break;
			default:
				event.setEventStatus(EventStatusEnum.DONE);
				break;
			}
		} catch (Throwable e) {
			event.setThrowable(e);
			event.setEventStatus(EventStatusEnum.FAILED);
		}
	}

	/**
	 * 异步接收并处理事件
	 * <p>
	 * 将事件处理任务提交到线程池中异步执行，避免阻塞主线程
	 *
	 * @param event     事件发布结果对象
	 * @param publisher 事件发布者
	 */
	public void reciveAsync(IPublishResult event, IPublisher publisher) {
		threadPoolManager.submit(SimpleThreadRequest.Builder.create("AbstractSubscriber-" + getSubscriberId(), () -> {
			recive(event);
		}).build(), null);
	}
}
