package com.tbs.transaction;

/**
 * 事务信息
 *
 * @author abstergo
 */
public interface ITransactionInfo {
    /**
     * 获取事务ID
     *
     * @return
     */
    String getTransactionId();

    /**
     * 获取事务类型
     *
     * @return
     */
    ITransaction getTransactionType();

    /**
     * 获取事务状态
     *
     * @return
     */
    TransactionStatus getTransactionStatus();

    /**
     * 设置事务状态
     *
     * @param transactionStatus
     */
    void setTransactionStatus(TransactionStatus transactionStatus);

}
