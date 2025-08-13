package com.tbs.transaction;

import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.lock.ILock;
import com.tbs.lock.impls.DelayDisposeLock;
import com.tbs.lock.util.LockUtil;
import com.tbs.schedule.ISchedule;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;

/**
 * 抽象的短锁事务
 *
 * @author abstergo
 */
public abstract class AbstractShortTermLockTransaction extends AbstractTransaction {

    @Lazy
    @Resource
    private ISchedule schedule;

    /**
     * 锁解锁延时
     */
    @Getter
    @Setter
    private long unlockDelayMs;

    @Override
    protected ILock getLock(String key) {
        return new DelayDisposeLock(schedule, LockUtil.getInstance().getConditionLockByStr(key), getUnlockDelayMs());
    }

    @Override
    protected String genKey(String transactionId) {
        return "transaction:" + EncryptUitl.hash256Encrypt(transactionId, "transaction:lock:salt");
    }
    
}
