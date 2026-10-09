package com.tbs.lock.redis;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import jakarta.annotation.Resource;

import com.tbs.lock.IConditionLock;
import com.tbs.lock.ILock;
import com.tbs.lock.ILockProvider;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;

import com.tbs.common.error.AssertUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于Redisson的Redis分布式锁提供者
 * <p>
 * 实现ILockProvider接口，提供分布式锁和条件锁的创建功能
 * 使用Redisson客户端实现分布式锁逻辑
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RedissonLockProvider implements ILockProvider {

	@Resource
	private RedissonClient redissonClient;

	/**
	 * 基于Redisson的Condition实现
	 * <p>
	 * 实现Condition接口，使用Redisson的CountDownLatch实现条件等待和通知功能
	 * </p>
	 */
	@Slf4j
	public static class RedissonLockCondition implements Condition {
		private final String lockKey;
		private final String conditionKey;
		private final RedissonClient redissonClient;

		/**
		 * 构造方法
		 *
		 * @param lockKey 锁的键名
		 * @param redissonClient Redisson客户端实例
		 */
		public RedissonLockCondition(String lockKey, RedissonClient redissonClient) {
			this.lockKey = lockKey;
			this.conditionKey = lockKey + ":condition";
			this.redissonClient = redissonClient;
		}

		/**
		 * 等待条件满足
		 * <p>
		 * 创建或获取CountDownLatch，初始化计数为1，然后等待计数归零
		 * </p>
		 *
		 * @throws InterruptedException 如果当前线程被中断
		 */
		@Override
		public void await() throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			// 初始化计数为1
			latch.trySetCount(1);
			// 等待计数归零
			latch.await();
		}

		/**
		 * 等待条件满足（不可中断）
		 * <p>
		 * 创建或获取CountDownLatch，初始化计数为1，然后等待计数归零
		 * 如果被中断，设置中断标志并记录日志
		 * </p>
		 */
		@Override
		public void awaitUninterruptibly() {
			try {
				RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
				latch.trySetCount(1);
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("InterruptedException", e);
			}
		}

		/**
		 * 在指定时间内等待条件满足
		 * <p>
		 * 创建或获取CountDownLatch，初始化计数为1，然后等待指定时间
		 * </p>
		 *
		 * @param nanosTimeout 超时时间（纳秒）
		 * @return 剩余时间（纳秒），如果超时返回-1
		 * @throws InterruptedException 如果当前线程被中断
		 */
		@Override
		public long awaitNanos(long nanosTimeout) throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			latch.trySetCount(1);
			boolean success = latch.await(nanosTimeout, TimeUnit.NANOSECONDS);
			return success ? 0L : -1L;
		}

		/**
		 * 在指定时间内等待条件满足
		 * <p>
		 * 创建或获取CountDownLatch，初始化计数为1，然后等待指定时间
		 * </p>
		 *
		 * @param time 超时时间
		 * @param unit 时间单位
		 * @return 是否在超时前条件满足
		 * @throws InterruptedException 如果当前线程被中断
		 */
		@Override
		public boolean await(long time, TimeUnit unit) throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			latch.trySetCount(1);
			return latch.await(time, unit);
		}

		/**
		 * 等待条件满足直到指定时间
		 * <p>
		 * 创建或获取CountDownLatch，初始化计数为1，然后等待直到指定时间
		 * </p>
		 *
		 * @param deadline 截止时间
		 * @return 是否在截止时间前条件满足
		 * @throws InterruptedException 如果当前线程被中断
		 */
		@Override
		public boolean awaitUntil( Date deadline) throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			latch.trySetCount(1);
			long timeout = deadline.getTime() - System.currentTimeMillis();
			return timeout > 0 && latch.await(timeout, TimeUnit.MILLISECONDS);
		}

		/**
		 * 通知一个等待线程条件已满足
		 * <p>
		 * 获取CountDownLatch并将计数减1，如果计数归零则释放等待线程
		 * </p>
		 */
		@Override
		public void signal() {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			// 减少计数，如果计数为0则释放等待线程
			try {
				latch.countDown();
			} catch (Exception e) {
				log.error("Exception", e);
			}
		}

		/**
		 * 通知所有等待线程条件已满足
		 * <p>
		 * 由于Redisson的CountDownLatch特性，此方法与signal()功能相同
		 * </p>
		 */
		@Override
		public void signalAll() {
			// Redisson的CountDownLatch只能countDown到0，因此signalAll与signal相同
			signal();
		}
	}

	/**
	 * 获取分布式锁实例
	 * <p>
	 * 根据目标对象创建RedissonLock实例
	 * </p>
	 *
	 * @param target 锁的目标对象
	 * @return 分布式锁实例
	 */
	@Override
	public ILock getLocker(Object target) {
		AssertUtil.notNull(target, "lock target can not be null");
		return new RedissonLock(target.toString(), redissonClient);
	}

	/**
	 * 获取带条件的分布式锁实例
	 * <p>
	 * 根据目标对象创建RedissonLock和RedissonLockCondition实例，然后返回IConditionLock实现
	 * </p>
	 *
	 * @param target 锁的目标对象
	 * @return 带条件的分布式锁实例
	 * @throws UnsupportedOperationException 如果不支持条件锁
	 */
	@Override
	public IConditionLock getConditionLocker(Object target) throws UnsupportedOperationException {
		AssertUtil.notNull(target, "lock target can not be null");
		String lockKey = target.toString();
		RedissonLock lock = new RedissonLock(lockKey, redissonClient);
		RedissonLockCondition condition = new RedissonLockCondition(lockKey, redissonClient);

		return new IConditionLock() {
			@Override
			public Condition newCondition() {
				return condition;
			}

			@Override
			public boolean tryLock(Duration timeOut) {
				return lock.tryLock(timeOut);
			}

			@Override
			public boolean isHeldByCurrentThread() {
				return lock.isHeldByCurrentThread();
			}

			@Override
			public boolean isLocked() {
				return lock.isLocked();
			}

			@Override
			public void unLock() {
				lock.unLock();
			}
		};
	}
}
