package com.tbs.lock.impls;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.lock.ILock;
import com.tbs.schedule.ISchedule;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * 支持延迟解锁的锁
 *
 * @author abstergo
 */
public class DelayDisposeLock implements ILock, IFeatureWrapper<ILock> {

    @Getter
    @Setter
    private ISchedule schedule;
    private ILock lock;

    /**
     * 解锁延迟时间
     */
    @Getter
    @Setter
    private long unlockDelayMs = 100;

    public DelayDisposeLock(ISchedule schedule, ILock lock, long unlockDelayMs) {
        this.schedule = schedule;
        this.lock = lock;
        this.unlockDelayMs = unlockDelayMs;
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
        schedule.pin(lock::unLock, Duration.ofMillis(getUnlockDelayMs()));
    }

    @Override
    public ILock getOrginal() {
        return lock;
    }

    @Override
    public void setOrginal(ILock orginal) {
        this.lock = orginal;
    }
}
