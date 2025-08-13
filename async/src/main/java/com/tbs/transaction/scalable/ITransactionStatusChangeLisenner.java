package com.tbs.transaction.scalable;

import com.tbs.transaction.ITransactionInfo;

/**
 * 事务状态变更监听器接口
 * <p>
 * 该接口定义了事务在不同状态变更时的回调方法，包括提交、回滚和完成三种状态变更事件。 实现类可以监听这些事件并在相应时机执行自定义逻辑。
 * </p>
 *
 * @see com.tbs.transaction.ITransactionInfo 事务信息接口
 * @see com.tbs.transaction.AbstractTransaction#commit(ITransactionInfo) 事务提交方法
 * @see com.tbs.transaction.AbstractTransaction#rollback(ITransactionInfo)
 *      事务回滚方法
 * @see com.tbs.transaction.AbstractTransaction#complete(ITransactionInfo)
 *      事务完成方法
 */
public interface ITransactionStatusChangeLisenner {

	/**
	 * 当事务被提交时调用此方法
	 * <p>
	 * 在{@link com.tbs.transaction.AbstractTransaction#commit(ITransactionInfo)}方法执行过程中，
	 * 当事务状态变更为COMMIT后会触发此回调方法。
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象，包含当前事务的完整信息
	 * @see com.tbs.transaction.TransactionStatus#COMMIT 事务提交状态
	 * @see com.tbs.transaction.ITransaction#commit(ITransactionInfo) 事务提交接口方法
	 */
	void onCommit(ITransactionInfo transactionInfo);

	/**
	 * 当事务被回滚时调用此方法
	 * <p>
	 * 在{@link com.tbs.transaction.AbstractTransaction#rollback(ITransactionInfo)}方法执行过程中，
	 * 当事务状态变更为ROLLBACK后会触发此回调方法。
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象，包含当前事务的完整信息
	 * @see com.tbs.transaction.TransactionStatus#ROLLBACK 事务回滚状态
	 * @see com.tbs.transaction.ITransaction#rollback(ITransactionInfo) 事务回滚接口方法
	 */
	void onRollback(ITransactionInfo transactionInfo);

	/**
	 * 当事务被完成时调用此方法
	 * <p>
	 * 在{@link com.tbs.transaction.AbstractTransaction#complete(ITransactionInfo)}方法执行过程中，
	 * 当事务状态变更为COMPLETE后会触发此回调方法。
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象，包含当前事务的完整信息
	 * @see com.tbs.transaction.TransactionStatus#COMPLETE 事务完成状态
	 * @see com.tbs.transaction.ITransaction#complete(ITransactionInfo) 事务完成接口方法
	 */
	void onComplete(ITransactionInfo transactionInfo);
}
