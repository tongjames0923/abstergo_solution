package com.tbs.broadcast.bus;

import java.time.Duration;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import com.tbs.common.IStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tbs.broadcast.enums.EventStatusEnum;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.lock.ILock;
import com.tbs.lock.util.LockUtil;

/**
 * 抽象事件总线实现类
 * <p>
 * 该类是事件总线的基础实现，提供了事件入队、出队和队列管理等核心功能的通用逻辑。 具体的事件总线实现需要继承该类并实现抽象方法。该类通过锁机制保证线程安全，
 * 并通过模板方法模式定义了事件处理的标准流程。
 *
 * @author tongj
 * @see IEventBus 事件总线接口
 * @see IPublishResult 事件发布结果接口
 */
public abstract class AbstractEventBus<T> implements IEventBus, IStartup {
	/**
	 * 获取事件队列
	 * <p>
	 * 返回用于存储事件发布结果的队列实例，具体的队列实现由子类决定， 可以是普通队列、优先级队列或其他自定义队列实现。
	 *
	 * @return 事件队列实例
	 * @see Queue 队列接口
	 * @see IPublishResult 事件发布结果接口
	 */
	protected abstract Queue<IPublishResult> getQueue();

	/**
	 * 日志记录器，用于记录事件总线的操作日志
	 */
	private static Logger logger = LoggerFactory.getLogger(AbstractEventBus.class);

	/**
	 * 锁对象，用于保证对事件队列的线程安全访问
	 * <p>
	 * 通过LockUtil获取与当前类相关的锁实例，确保在多线程环境下的数据一致性。
	 */
	ILock lock = null;

    @Override
    public void startUp() throws RuntimeException {
        logger.debug("初始化事件总线");
        lock = LockUtil.getInstance().getLockerByClass(this.getClass());
    }

    /**
	 * 发送队列信号
	 * <p>
	 * 当有新事件入队时调用此方法通知等待事件的线程。具体的信号机制由子类实现， 可以是信号量、条件变量或其他同步机制。
	 *
	 * @see #inqueueEvent(IPublishResult) 事件入队方法
	 */
	protected abstract void signalInQueue();

	/**
	 * 等待队列条件满足
	 * <p>
	 * 当需要获取事件但队列为空时调用此方法等待新事件的到来。具体的等待机制由子类实现， 可以是信号量、条件变量或其他同步机制。
	 *
	 * @param timeout 等待超时时间
	 * @param unit    超时时间单位
	 * @return 如果在超时时间内条件满足返回true，否则返回false
	 * @throws InterruptedException 如果等待过程中线程被中断则抛出该异常
	 * @see #nextUnhandledEvent(Duration) 获取未处理事件方法
	 * @see TimeUnit 时间单位枚举
	 */
	protected abstract boolean awaitQueue(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * 将事件发布结果添加到队列中
	 * <p>
	 * 实现IEventBus接口的事件入队方法。首先对事件进行前置检查，然后使用锁保护队列操作， 将事件添加到队列中并发送队列信号通知等待的线程。
	 *
	 * @param publishResult 事件发布结果对象，不能为null
	 * @see IEventBus#inqueueEvent(IPublishResult) 事件总线入队接口方法
	 * @see #preInqueueCheck(IPublishResult) 事件入队前置检查方法
	 * @see #getQueue() 获取队列方法
	 * @see #signalInQueue() 发送队列信号方法
	 */
	@Override
	public void inqueueEvent(IPublishResult publishResult) {
		preInqueueCheck(publishResult);
		LockUtil.getInstance().lockThen(lock, () -> {
			getDistributedBusSupport().ifPresentOrElse(distributedBusSupport -> {
				distributedBusSupport.broadcastEvent(distributedBusSupport.getDataBridge().bridge(publishResult));
			}, () -> {
				logger.debug("添加事件到队列:{}", publishResult.getEvent().getEventName());
				getQueue().add(publishResult);
				signalInQueue();
			});

		});
	}

	@Override
	public Optional<IDistributedBusSupport> getDistributedBusSupport() {
		return Optional.empty();
	}

	/**
	 * 事件入队前置检查
	 * <p>
	 * 对即将入队的事件发布结果进行验证检查，确保事件对象不为null， 并且事件状态为允许入队的状态（IDLE、REQUIRE_RETRY）。
	 * 如果状态检查通过，则将事件状态设置为IN_QUEUE（等待处理）状态。
	 *
	 * @param publishResult 事件发布结果对象，不能为null
	 * @param <T>           泛型参数
	 * @throws FormattedRuntimeException 当事件状态不正确时抛出
	 * @throws IllegalArgumentException  当publishResult或event为null时抛出
	 * @see EventStatusEnum 事件状态枚举
	 * @see AssertUtil 断言工具类
	 */
	protected static <T> void preInqueueCheck(IPublishResult publishResult) {
		AssertUtil.notNull(publishResult, "publishResult can not be null");
		AssertUtil.notNull(publishResult.getEvent(), "event can not be null");
		if (publishResult.getEventStatus() == null || publishResult.getEventStatus() == EventStatusEnum.IDLE
				|| publishResult.getEventStatus() == EventStatusEnum.REQUIRE_RETRY) {
			publishResult.setEventStatus(EventStatusEnum.IN_QUEUE);
		} else {
			AssertUtil.throwError(() -> new FormattedRuntimeException("事件状态错误,入队的状态仅允许IDLE，REQUIRE_RETRY,IN_QUEUE"));
		}
	}

	/**
	 * 获取下一个未处理的事件
	 * <p>
	 * 实现IEventBus接口的获取未处理事件方法。如果队列为空则等待新事件到来，
	 * 否则直接从队列中取出一个事件并将其状态设置为IN_PROCESS（处理中）。
	 *
	 * @param duration 等待超时时间
	 * @return 事件发布结果对象，如果超时则返回null
	 * @see IEventBus#nextUnhandledEvent(Duration) 事件总线获取未处理事件接口方法
	 * @see #awaitQueue(long, TimeUnit) 等待队列方法
	 * @see #getQueue() 获取队列方法
	 * @see EventStatusEnum#IN_PROCESS 处理中状态
	 */
	@Override
	public IPublishResult nextUnhandledEvent(Duration duration) {
		IPublishResult[] result = new IPublishResult[1];
		if (getQueue().isEmpty()) {
			logger.debug("等待事件");
			boolean r = false;
			try {
				r = awaitQueue(duration.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.error("条件等待异常", e);
				Thread.currentThread().interrupt();
			}
			logger.debug("等待事件结束，是否超时：{}", !r);
		}
		if (!getQueue().isEmpty()) {
			LockUtil.getInstance().lockThen(lock, () -> {
				result[0] = getQueue().poll();
				if (result[0] != null) {
					logger.debug("获取事件:{}", result[0].getEvent().getEventId());
				}
			});
		}

		if (result[0] != null) {
			result[0].setEventStatus(EventStatusEnum.IN_PROCESS);
		}
		return result[0];
	}

	/**
	 * 获取事件队列大小
	 * <p>
	 * 实现IEventBus接口的获取队列大小方法，返回当前事件队列中等待处理的事件数量。
	 *
	 * @return 事件队列中的事件数量
	 * @see IEventBus#getEventQueueSize() 事件总线获取队列大小接口方法
	 * @see #getQueue() 获取队列方法
	 */
	@Override
	public int getEventQueueSize() {
		return getQueue().size();
	}
}
