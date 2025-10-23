package com.tbs.broadcast.dispatcher;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.broadcast.bus.IEventBus;
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
	 * 获取所有订阅者
	 * <p>
	 * 返回当前分发器注册的订阅者列表
	 *
	 * @return 订阅者列表
	 */
	public Collection<AbstractSubscriber> getSubscribers() {
		return subscribersHolder.values();
	}

	/**
	 * 处理事件
	 * <p>
	 * 根据事件分发规则，将事件分发给相应的订阅者进行处理
	 *
	 * @param result 要处理的事件结果，不能为null
	 * @throws IllegalArgumentException 当result为null时抛出
	 */
	public void pollEventHandle() {
		if (started) {
			return;
		}
		started = true;
		logger.info("开始处理事件");
		while (started) {
			ValueWrapper<IPublishResult> iPublishResult = new ValueWrapper<>();
			try {
				iPublishResult.set(getEventBus().nextUnhandledEvent(Duration.ofSeconds(15)));
				iPublishResult.get().ifPresentOrElse((v) -> {
					handleEvent(v);
				}, () -> {
					logger.trace("空事件超时");
				});

			} catch (Throwable e) {
				handleError(e, iPublishResult.get().orElse(null));
			}
		}
		logger.info("停止处理事件");
	}

	/**
	 * 停止处理事件
	 * <p>
	 * 停止事件处理流程，并释放相关资源。
	 */
	public void stopPolling() {
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
		pollEventHandle();
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
		stopPolling();
	}
}
