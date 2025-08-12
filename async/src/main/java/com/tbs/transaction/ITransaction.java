package com.tbs.transaction;

import java.util.Optional;

/**
 * 事务接口
 *
 * @author abstergo
 */
public interface ITransaction {
    /**
     * 开启事务
     *
     * @param transactionId
     * @return
     */
    ITransactionInfo beginTransaction(String transactionId);

    /**
     * 提交事务
     *
     * @param transactionInfo
     */
    void commit(ITransactionInfo transactionInfo);

    /**
     * 回滚事务
     *
     * @param transactionInfo
     */
    void rollback(ITransactionInfo transactionInfo);

    /**
     * 完成事务
     *
     * @param transactionInfo
     */
    void complete(ITransactionInfo transactionInfo);

    /**
     * 获取事务信息
     *
     * @param key
     * @return
     */
    Optional<ITransactionInfo> getTransaction(String key);

}
