package com.tbs.transaction.scalable;

import com.tbs.transaction.ITransactionInfo;

/**
 * 事务冲突解决器接口
 * <p>
 * 该接口定义了处理事务冲突的方法。当尝试在已存在的事务上创建新事务时， 系统会调用此接口的实现来解决冲突。
 * </p>
 *
 * @see AbstractScalableTransaction#beginOnhasTransaction(ITransactionInfo)
 * @see AbstractScalableTransaction#getTransactionConflictSolver()
 */
public interface ITransactionConflictSolver {

	/**
	 * 当在已存在的事务上创建新事务时调用此方法
	 * <p>
	 * 此方法用于处理事务冲突情况，允许实现类定义特定的冲突解决策略。 例如，可以合并事务、拒绝新事务或采用其他业务逻辑处理方式。
	 * </p>
	 *
	 * @param transactionInfo 已存在的事务信息对象
	 * @see ITransactionInfo 事务信息接口
	 * @see AbstractScalableTransaction#beginOnhasTransaction(ITransactionInfo)
	 */
	void onCreateTransactionOnExistingTransaction(ITransactionInfo transactionInfo);
}
