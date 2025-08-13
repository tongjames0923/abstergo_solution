package com.tbs.transaction.scalable.info;

import java.io.Serializable;

/**
 * 可回滚数据接口
 * <p>
 * 该接口定义了支持事务回滚和提交操作的数据对象。 实现类需要提供数据获取、提交和回滚的功能，以支持事务的一致性操作。
 * </p>
 *
 * @see com.tbs.transaction.scalable.info.DefaultTransactionInfo 事务信息默认实现类
 */
public interface IRollbackableData extends Serializable {
	/**
	 * 回滚数据
	 * <p>
	 * 该方法用于撤销对数据的更改，将数据恢复到事务开始之前的状态。
	 * </p>
	 *
	 * @see com.tbs.transaction.ITransaction#rollback(com.tbs.transaction.ITransactionInfo)
	 * @see com.tbs.transaction.TransactionStatus#ROLLBACK
	 */
	void rollback();

	/**
	 * 提交数据
	 * <p>
	 * 该方法用于确认对数据的更改，使数据更改永久生效。
	 * </p>
	 *
	 * @param data 需要提交的数据对象
	 * @see com.tbs.transaction.ITransaction#commit(com.tbs.transaction.ITransactionInfo)
	 * @see com.tbs.transaction.TransactionStatus#COMMIT
	 */
	void commit(Serializable data);

	/**
	 * 获取数据
	 * <p>
	 * 该方法用于获取当前数据对象的值。
	 * </p>
	 *
	 * @return Object 当前数据对象的值
	 */
	Serializable getData();
}
