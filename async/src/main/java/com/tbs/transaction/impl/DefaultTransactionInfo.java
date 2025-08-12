package com.tbs.transaction.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.transaction.ITransaction;
import com.tbs.transaction.ITransactionInfo;
import com.tbs.transaction.TransactionStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 默认事务信息
 *
 * @author abstergo
 */
public class DefaultTransactionInfo implements ITransactionInfo {

    private String transactionId;

    private ITransaction transactionType;

    private TransactionStatus transactionStatus;

    private TransactionStatus tra;

    public DefaultTransactionInfo(@NotNull String transactionId, @NotNull ITransaction transactionType) {

        AssertUtil.hasText(transactionId, "transactionId can not be null or empty");
        AssertUtil.notNull(transactionType, "transactionType can not be null");
        this.transactionId = transactionId;
        this.transactionType = transactionType;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public ITransaction getTransactionType() {
        return transactionType;
    }

    @Override
    public TransactionStatus getTransactionStatus() {
        return tra;
    }

    @Override
    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
