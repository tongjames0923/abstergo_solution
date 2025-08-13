package com.tbs.lock.impls;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.lock.IConditionLock;

/**
 * @author Abstergo
 */
public class LockAdapter implements IConditionLock, IFeatureWrapper<Lock> {

	private ThreadLocal<Boolean> flag = new InheritableThreadLocal<Boolean>();
	private AtomicBoolean isLock = new AtomicBoolean(false);
	private Lock lock;

	public LockAdapter(Lock lock) {
		this.lock = lock;
	}

	@Override
	public boolean tryLock(Duration timeOut) {
		try {
			boolean f = lock.tryLock(timeOut == null ? 1000 * 60 * 30L : timeOut.toMillis(),
					java.util.concurrent.TimeUnit.MILLISECONDS);
			if (f) {
				flag.set(true);
			}
			isLock.set(f);
			return f;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	@Override
	public boolean isHeldByCurrentThread() {
		return flag.get() == null ? false : flag.get();
	}

	@Override
	public boolean isLocked() {
		return isLock.get();
	}

	@Override
	public void unLock() {
		lock.unlock();
		flag.remove();
		isLock.set(false);
	}

	@Override
	public Condition newCondition() {
		return lock.newCondition();
	}

	@Override
	public Lock getOrginal() {
		return lock;
	}

	@Override
	public void setOrginal(Lock orginal) {
		this.lock = orginal;
	}
}
