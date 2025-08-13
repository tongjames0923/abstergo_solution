package com.tbs.transaction.scalable;

import com.tbs.transaction.ITransactionInfo;

import java.util.Optional;

/**
 * 事务存储接口，用于存储和获取事务信息
 */
public interface ITransactionStore {
    /**
     * 存储事务信息
     *
     * @param key             事务键
     * @param transactionInfo 事务信息
     * @return boolean 存储是否成功
     */
    boolean store(String key, ITransactionInfo transactionInfo);

    /**
     * 获取事务信息
     *
     * @param key 事务键
     * @return Optional<ITransactionInfo> 事务信息包装对象
     */
    Optional<ITransactionInfo> get(String key);
}
