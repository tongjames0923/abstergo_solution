package com.tbs.transaction.scalable.info;

import java.io.Serializable;
import java.util.Optional;

import com.tbs.common.error.AssertUtil;
import com.tbs.transaction.ITransactionInfo;
import com.tbs.transaction.TransactionStatus;

/**
 * 抽象事务信息类
 * <p>
 * 该类实现了{@link ITransactionInfo}接口并继承了{@link Serializable}接口，
 * 提供了事务信息的基本实现，包括事务ID和事务状态的管理。 同时定义了处理可回滚数据的抽象方法，子类需要实现这些方法以提供具体的数据处理逻辑。
 * </p>
 *
 * @see ITransactionInfo 事务信息接口
 * @see IRollbackableData 可回滚数据接口
 * @see TransactionStatus 事务状态枚举
 */
public abstract class AbstractRollbackableTransactionInfo implements ITransactionInfo, Serializable {
	private static final long serialVersionUID = 1L;
	private String transactionId;
	private TransactionStatus transactionStatus = TransactionStatus.BEGIN;

	/**
	 * 获取事务ID
	 * <p>
	 * 该方法返回当前事务的唯一标识符。
	 * </p>
	 *
	 * @return String 事务ID
	 * @see ITransactionInfo#getTransactionId()
	 */
	@Override
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * 获取事务状态
	 * <p>
	 * 该方法返回当前事务的状态，初始状态为BEGIN。
	 * </p>
	 *
	 * @return TransactionStatus 事务状态枚举值
	 * @see ITransactionInfo#getTransactionStatus()
	 * @see TransactionStatus 事务状态枚举
	 */
	@Override
	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}

	/**
	 * 设置事务状态
	 * <p>
	 * 该方法用于更新事务的状态。
	 * </p>
	 *
	 * @param transactionStatus 新的事务状态
	 * @see ITransactionInfo#setTransactionStatus(TransactionStatus)
	 * @see TransactionStatus 事务状态枚举
	 */
	@Override
	public void setTransactionStatus(TransactionStatus transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	/**
	 * 抽取指定键的可回滚数据
	 * <p>
	 * 该抽象方法由子类实现，用于根据指定键获取已存在的可回滚数据。
	 * </p>
	 *
	 * @param key 数据键
	 * @return Optional<IRollbackableData> 可回滚数据的可选包装，如果不存在则返回空Optional
	 * @see IRollbackableData 可回滚数据接口
	 */
	protected abstract Optional<IRollbackableData> pollData(String key);

	/**
	 * 创建指定键的可回滚数据
	 * <p>
	 * 该抽象方法由子类实现，用于根据指定键创建新的可回滚数据对象。
	 * </p>
	 *
	 * @param key 数据键
	 * @return IRollbackableData 新创建的可回滚数据对象
	 * @see IRollbackableData 可回滚数据接口
	 */
	protected abstract IRollbackableData createRollbackableData(String key);

	/**
	 * 设置指定键的数据
	 * <p>
	 * 该方法用于提交指定键的数据。如果该键已存在可回滚数据，则直接提交； 否则创建新的可回滚数据对象并提交数据。
	 * </p>
	 *
	 * @param key  数据键
	 * @param data 需要提交的数据
	 * @throws IllegalArgumentException 当key为空或data为null时抛出
	 * @see IRollbackableData#commit(Object) 可回滚数据提交方法
	 * @see #pollData(String) 抽取可回滚数据方法
	 * @see #createRollbackableData(String) 创建可回滚数据方法
	 */
	public void setData(String key, Serializable data) {
		AssertUtil.hasText(key, "key不能为空");
		AssertUtil.notNull(data, "rollbackableData不能为空");
		pollData(key).ifPresentOrElse((r) -> {
			r.commit(data);
		}, () -> {
			IRollbackableData rollbackableData = createRollbackableData(key);
			rollbackableData.commit(data);
		});
	}

	/**
	 * 获取指定键的可回滚数据
	 * <p>
	 * 该方法用于获取指定键的可回滚数据对象。如果该键已存在可回滚数据，则直接返回； 否则创建新的可回滚数据对象并返回。
	 * </p>
	 *
	 * @param key 数据键
	 * @return IRollbackableData 指定键的可回滚数据对象
	 * @throws IllegalArgumentException 当key为空时抛出
	 * @see IRollbackableData 可回滚数据接口
	 * @see #pollData(String) 抽取可回滚数据方法
	 * @see #createRollbackableData(String) 创建可回滚数据方法
	 */
	public IRollbackableData getData(String key) {
		AssertUtil.hasText(key, "key不能为空");
		return pollData(key).orElseGet(() -> {
			return createRollbackableData(key);
		});
	}
}
