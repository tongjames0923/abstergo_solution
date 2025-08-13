package com.tbs.transaction;

import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.lock.ILock;
import com.tbs.lock.util.LockUtil;
import com.tbs.transaction.impl.DefaultTransactionInfo;

import java.util.Optional;

/**
 * @author abstergo
 */

public abstract class AbstractTransaction implements ITransaction {

    /**
     * 获取锁
     *
     * @param key
     * @return
     */
    protected abstract ILock getLock(String key);

    /**
     * 生成事务信息key
     *
     * @param transactionId
     * @return
     */
    protected abstract String genKey(String transactionId);

    /**
     * 获取事务信息
     *
     * @param transactionId
     * @return
     */
    protected abstract Optional<ITransactionInfo> getTransactionInfo(String transactionId);

    /**
     * 启动事务时如果有事务，则调用此方法
     *
     * @param transactionId
     */
    protected void beginOnhasTransaction(ITransactionInfo transactionInfo) {

    }

    protected ITransactionInfo createTransactionInfo(String transactionId) {
        ITransactionInfo transactionInfo = new DefaultTransactionInfo(transactionId, this);
        transactionInfo.setTransactionStatus(TransactionStatus.BEGIN);
        return transactionInfo;
    }

    /**
     * 事务持久化
     *
     * @param transactionInfo
     * @return
     */
    protected abstract boolean persistent(ITransactionInfo transactionInfo);

    protected void completeOnhasTransaction(ITransactionInfo transactionInfo) {

    }

    private Optional<ITransactionInfo> doSomething(String transactionId, IDoTransaction doTransaction) {
        ValueWrapper<ITransactionInfo> transactionInfo = new ValueWrapper<>();
        String key = genKey(transactionId);
        LockUtil.getInstance().lockThen(getLock(key), () -> {
            doTransaction.doTransaction(key, transactionInfo);
        });
        return transactionInfo.get();
    }

    @Override
    public ITransactionInfo beginTransaction(String transactionId) {
        return doSomething(transactionId, (key, transactionInfo) -> {
            getTransactionInfo(transactionId).ifPresentOrElse(t -> {
                beginOnhasTransaction(t);
                transactionInfo.set(t);
            }, () -> {
                transactionInfo.set(createTransactionInfo(transactionId));
            });
            persistent(transactionInfo.get()
                .orElseThrow(() -> new FormattedRuntimeException("{} 创建事务信息失败", transactionId)));
        }).orElseThrow(() -> new FormattedRuntimeException("{} 获取事务信息失败", transactionId));
    }

    @Override
    public void commit(ITransactionInfo transactionInfo) {
        doSomething(transactionInfo.getTransactionId(), (id, r) -> {
            getTransactionInfo(id).ifPresentOrElse(t -> {
                AssertUtil.isTrue(!t.getTransactionStatus().getCode().equals(TransactionStatus.BEGIN.getCode()),
                    "{} 事务状态异常。当前状态 {}", id, t.getTransactionStatus().getValue());
                t.setTransactionStatus(TransactionStatus.COMMIT);
                persistent(t);
            }, () -> {
                AssertUtil.throwError(() -> {
                    return new FormattedRuntimeException("{} 事务不存在,无法提交", id);
                });
            });
        });
    }

    @Override
    public void rollback(ITransactionInfo transactionInfo) {
        doSomething(transactionInfo.getTransactionId(), (id, r) -> {
            getTransactionInfo(id).ifPresentOrElse(t -> {
                AssertUtil.isTrue(!t.getTransactionStatus().getCode().equals(TransactionStatus.BEGIN.getCode()),
                    "{} 事务状态异常。当前状态 {}", id, t.getTransactionStatus().getValue());
                t.setTransactionStatus(TransactionStatus.ROLLBACK);
                persistent(t);
            }, () -> {
                AssertUtil.throwError(() -> {
                    return new FormattedRuntimeException("{} 事务不存在,无法回滚", id);
                });
            });
        });
    }

    @Override
    public void complete(ITransactionInfo transactionInfo) {
        doSomething(transactionInfo.getTransactionId(), (id, r) -> {
            getTransactionInfo(id).ifPresentOrElse(t -> {
                completeOnhasTransaction(t);
            }, () -> {
                AssertUtil.throwError(() -> {
                    return new FormattedRuntimeException("{} 获取事务信息失败", id);
                });
            });
        });
    }

    @Override
    public Optional<ITransactionInfo> getTransaction(String key) {
        return getTransactionInfo(key);
    }

    private interface IDoTransaction {
        /**
         * 执行事务
         *
         * @param key
         * @param transactionInfo
         */
        void doTransaction(String key, ValueWrapper<ITransactionInfo> transactionInfo);
    }
}
