package com.tbs.transaction.scalable;

import com.tbs.transaction.ITransactionInfo;

/**
 * 事务边界控制器接口
 * <p>
 * 该接口定义了在事务状态变更时处理事务边界操作的方法。 实现类可以在此处添加日志记录、监控、通知等边界处理逻辑。
 * </p>
 *
 * @author jiangnan
 * @date 2021/11/09
 * @see AbstractScalableTransaction#getTransactionEdgeController()
 * @see AbstractScalableTransaction#edgeControll(java.util.Optional)
 */
public interface ITransactionEdgeController {
	/**
	 * 处理创建时产生的事务边界
	 * <p>
	 * 当事务状态变更为BEGIN时调用此方法，用于处理事务创建时的边界操作。
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象，包含当前事务的状态和相关信息
	 * @see ITransactionInfo 事务信息接口
	 * @see com.tbs.transaction.TransactionStatus#BEGIN 事务开始状态
	 * @see AbstractScalableTransaction#edgeControll(java.util.Optional)
	 */
	void onCreateEdge(ITransactionInfo transactionInfo);

	/**
	 * 处理提交时产生事务边界
	 * <p>
	 * 当事务状态变更为COMMIT时调用此方法，用于处理事务提交时的边界操作。
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象，包含当前事务的状态和相关信息
	 * @see ITransactionInfo 事务信息接口
	 * @see com.tbs.transaction.TransactionStatus#COMMIT 事务提交状态
	 * @see AbstractScalableTransaction#edgeControll(java.util.Optional)
	 */
	void onCommitEdge(ITransactionInfo transactionInfo);

	/**
	 * 处理回滚时产生事务边界
	 * <p>
	 * 当事务状态变更为ROLLBACK时调用此方法，用于处理事务回滚时的边界操作。
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象，包含当前事务的状态和相关信息
	 * @see ITransactionInfo 事务信息接口
	 * @see com.tbs.transaction.TransactionStatus#ROLLBACK 事务回滚状态
	 * @see AbstractScalableTransaction#edgeControll(java.util.Optional)
	 */
	void onRollbackEdge(ITransactionInfo transactionInfo);

	/**
	 * 处理完成时产生事务边界
	 * <p>
	 * 当事务状态变更为COMPLETE时调用此方法，用于处理事务完成时的边界操作。
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象，包含当前事务的状态和相关信息
	 * @see ITransactionInfo 事务信息接口
	 * @see com.tbs.transaction.TransactionStatus#COMPLETE 事务完成状态
	 * @see AbstractScalableTransaction#edgeControll(java.util.Optional)
	 */
	void onCompleteEdge(ITransactionInfo transactionInfo);
}
