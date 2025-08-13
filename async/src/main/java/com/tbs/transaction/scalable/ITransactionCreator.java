package com.tbs.transaction.scalable;

import com.tbs.transaction.ITransactionInfo;

/**
 * 事务创建器接口，用于创建事务信息对象
 * <p>
 * 该接口定义了创建事务信息对象的标准方法。实现类需要根据给定的事务ID创建并初始化 一个新的事务信息对象，通常设置初始状态为BEGIN。
 * </p>
 *
 * @see ITransactionInfo 事务信息接口
 * @see AbstractScalableTransaction#createTransactionInfo(String)
 * @see AbstractScalableTransaction#getTransactionCreator()
 */
public interface ITransactionCreator {

	/**
	 * 根据事务ID创建事务信息对象
	 * <p>
	 * 此方法负责创建一个新的事务信息对象，并将其与指定的事务ID关联。 创建的事务信息对象通常包含事务ID、事务类型和初始事务状态等基本信息。
	 * </p>
	 *
	 * @param transactionId 事务ID，用于唯一标识一个事务
	 * @return ITransactionInfo 新创建的事务信息对象，初始状态通常为BEGIN
	 * @see ITransactionInfo 事务信息接口
	 * @see com.tbs.transaction.TransactionStatus 事务状态枚举
	 * @see AbstractScalableTransaction#createTransactionInfo(String)
	 */
	ITransactionInfo createTransactionInfo(String transactionId);
}
