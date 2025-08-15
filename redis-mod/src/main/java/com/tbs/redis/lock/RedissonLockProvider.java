package com.tbs.redis.lock;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;

import com.tbs.common.error.AssertUtil;
import com.tbs.lock.IConditionLock;
import com.tbs.lock.ILock;
import com.tbs.lock.ILockProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * redis锁提供者
 *
 * @author tongj
 */
public class RedissonLockProvider implements ILockProvider {

	@Resource
	private RedissonClient redissonClient;

	@Slf4j
	public static class RedissonLockCondition implements Condition {
		private final String lockKey;
		private final String conditionKey;
		private final RedissonClient redissonClient;

		public RedissonLockCondition(String lockKey, RedissonClient redissonClient) {
			this.lockKey = lockKey;
			this.conditionKey = lockKey + ":condition";
			this.redissonClient = redissonClient;
		}

		@Override
		public void await() throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			// 初始化计数为1
			latch.trySetCount(1);
			// 等待计数归零
			latch.await();
		}

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

		@Override
		public long awaitNanos(long nanosTimeout) throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			latch.trySetCount(1);
			boolean success = latch.await(nanosTimeout, TimeUnit.NANOSECONDS);
			return success ? 0L : -1L;
		}

		@Override
		public boolean await(long time, TimeUnit unit) throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			latch.trySetCount(1);
			return latch.await(time, unit);
		}

		@Override
		public boolean awaitUntil(@NotNull Date deadline) throws InterruptedException {
			RCountDownLatch latch = redissonClient.getCountDownLatch(conditionKey);
			latch.trySetCount(1);
			long timeout = deadline.getTime() - System.currentTimeMillis();
			return timeout > 0 && latch.await(timeout, TimeUnit.MILLISECONDS);
		}

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

		@Override
		public void signalAll() {
			// Redisson的CountDownLatch只能countDown到0，因此signalAll与signal相同
			signal();
		}
	}

	@Override
	public ILock getLocker(Object target) {
		AssertUtil.notNull(target, "lock target can not be null");
		return new RedissonLock(target.toString(), redissonClient);
	}

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
