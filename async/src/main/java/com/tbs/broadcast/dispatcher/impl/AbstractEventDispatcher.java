package com.tbs.broadcast.dispatcher.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.broadcast.bus.IEventBus;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.broadcast.subscriber.AbstractSubscriber;
import com.tbs.common.IStartup;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;

/**
 * 抽象事件分发器实现类
 * <p>
 * 该类是事件分发器的基础实现，提供了事件处理的核心逻辑。它实现了IEventDispatcher接口，
 * 并集成了系统启动(IStartup)和资源销毁(DisposableBean)功能。该类负责管理订阅者的注册与注销、
 * 事件的获取与分发、以及事件处理流程的控制等核心功能。
 *
 * @author abstergo
 */
public abstract class AbstractEventDispatcher implements IEventDispatcher, IStartup, DisposableBean {

	/**
	 * 事件总线，用于事件的排队和获取
	 */
	@Resource
	private IEventBus eventBus;

	/**
	 * 日志记录器
	 */
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractEventDispatcher.class);

	/**
	 * 事件处理状态标志，true表示正在处理事件，false表示已停止处理
	 */
	private boolean started = false;

	/**
	 * 订阅者持有映射表，用于存储和管理已注册的订阅者 key为订阅者ID，value为订阅者实例
	 */
	private Map<String, AbstractSubscriber> subscribersHolder = new HashMap<>(8);

	/**
	 * 注册订阅者
	 * <p>
	 * 将指定的订阅者添加到订阅者持有映射表中，以订阅者ID为键进行存储
	 *
	 * @param subscriber 要注册的订阅者实例，不能为null
	 * @throws IllegalArgumentException 当subscriber为null时抛出
	 */
	@Override
	public void register(@NotNull AbstractSubscriber subscriber) {
		AssertUtil.notNull(subscriber, "订阅者不能为空");
		subscribersHolder.put(subscriber.getSubscriberId(), subscriber);
	}

	/**
	 * 注销订阅者
	 * <p>
	 * 从订阅者持有映射表中移除指定的订阅者
	 *
	 * @param subscriber 要注销的订阅者实例，不能为null
	 * @throws IllegalArgumentException 当subscriber为null时抛出
	 */
	@Override
	public void unregister(AbstractSubscriber subscriber) {
		AssertUtil.notNull(subscriber, "订阅者不能为空");
		subscribersHolder.remove(subscriber.getSubscriberId());
	}

	/**
	 * 获取事件总线
	 * <p>
	 * 返回当前分发器使用的事件总线实例
	 *
	 * @return 事件总线实例
	 */
	@Override
	public IEventBus getEventBus() {
		return eventBus;
	}

	/**
	 * 处理事件
	 * <p>
	 * 启动事件处理主循环，持续从事件总线中获取未处理的事件并分发给匹配的订阅者进行处理。 处理过程会根据事件的处理模式（同步或异步）选择相应的处理方式。
	 * 该方法会一直运行直到调用stopHandle()方法停止处理。
	 */
	@Override
	public void handleEvent() {
		if (started) {
			return;
		}
		started = true;
		logger.trace("开始处理事件");
		while (started) {
			ValueWrapper<IPublishResult> iPublishResult = new ValueWrapper<>();
			try {
				iPublishResult.set(getEventBus().nextUnhandledEvent(Duration.ofMinutes(15)));
				if (iPublishResult == null) {
					logger.trace("空事件超时");
					continue;
				}
				iPublishResult.get().ifPresent(res -> {
					subscribersHolder.values().stream().filter((v) -> {
						return v != null && v instanceof AbstractSubscriber;
					}).map(v -> (AbstractSubscriber) v).forEach(subscriber -> {
						if (subscriber.supportEvent(res.getEvent().getEventId(), res.getEvent().getEventName())
								&& subscriber.supportPublisher(res.getPublisher())) {
							if (res.getHandleMode() == HandleMode.SYNC) {
								subscriber.recive(res);
							} else {
								subscriber.reciveAsync(res, res.getPublisher());
							}
						}
					});
				});

			} catch (Throwable e) {
				handleError(e, iPublishResult.get().orElse(null));
			}
		}
		logger.trace("停止处理事件");

	}

	/**
	 * 停止处理事件
	 * <p>
	 * 设置处理状态标志为false，使事件处理主循环在下一次迭代时退出， 从而停止事件处理流程。
	 */
	@Override
	public void stopHandle() {
		started = false;
	}

	/**
	 * 系统启动方法
	 * <p>
	 * 实现IStartup接口的启动方法，在系统启动时自动调用， 通过@LongTermAsync注解标识为长期异步执行的任务， 启动事件处理主流程。
	 *
	 * @throws RuntimeException 启动过程中可能抛出的运行时异常
	 */
	@Override
	@LongTermAsync
	public void startUp() throws RuntimeException {
		handleEvent();
	}

	/**
	 * 销毁方法
	 * <p>
	 * 实现DisposableBean接口的销毁方法，在容器关闭时自动调用， 用于停止事件处理流程，释放相关资源。
	 *
	 * @throws Exception 销毁过程中可能抛出的异常
	 */
	@Override
	public void destroy() throws Exception {
		stopHandle();
	}
}
