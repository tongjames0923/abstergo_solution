package com.tbs.transaction;

import java.util.Optional;

import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.lock.ILock;
import com.tbs.lock.util.LockUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 抽象事务类，提供事务管理的基本实现框架。
 *
 * <p>
 * 该类实现了{@link ITransaction}接口，提供了事务的完整生命周期管理，
 * 包括开始、提交、回滚和完成等操作。所有事务操作都会通过分布式锁保证原子性。
 * </p>
 *
 * <p>
 * 子类需要实现抽象方法来提供具体的锁机制、事务信息存储和持久化等实现。
 * </p>
 *
 * @author abstergo
 * @see ITransaction 事务接口
 * @see ITransactionInfo 事务信息接口
 * @see ILock 分布式锁接口
 * @see LockUtil 锁工具类
 */
@Slf4j
public abstract class AbstractTransaction implements ITransaction {

	/**
	 * 获取指定key的分布式锁实例
	 *
	 * @param key 锁的键名
	 * @return ILock 分布式锁实例
	 * @see ILock
	 */
	protected abstract ILock getLock(String key);

	/**
	 * 根据事务ID生成事务信息存储的key
	 *
	 * @param transactionId 事务ID
	 * @return String 事务信息存储key
	 */
	protected abstract String genKey(String transactionId);

	/**
	 * 根据事务ID获取事务信息
	 *
	 * @param transactionId 事务ID
	 * @return Optional<ITransactionInfo> 事务信息包装对象，如果不存在则返回空Optional
	 * @see ITransactionInfo
	 */
	protected abstract Optional<ITransactionInfo> getTransactionInfo(String transactionId);

	/**
	 * 当存在事务时，在开始事务前调用此方法
	 *
	 * <p>
	 * 默认实现为空，子类可重写此方法添加特定逻辑
	 * </p>
	 *
	 * @param transactionInfo 已存在的事务信息
	 * @see ITransactionInfo
	 */
	protected void beginOnhasTransaction(ITransactionInfo transactionInfo) {

	}

	/**
	 * 边界控制方法，在事务操作前后调用
	 *
	 * <p>
	 * 默认实现为空，子类可重写此方法添加边界控制逻辑，如日志记录、监控等
	 * </p>
	 *
	 * @param transactionInfo 事务信息包装对象
	 */
	protected void edgeControll(Optional<ITransactionInfo> transactionInfo) {
	}

	/**
	 * 创建新的事务信息对象
	 *
	 * @param transactionId 事务ID
	 * @return ITransactionInfo 新创建的事务信息对象
	 * @see DefaultTransactionInfo 默认事务信息实现
	 * @see ITransactionInfo
	 */
	protected abstract ITransactionInfo createTransactionInfo(String transactionId);

	/**
	 * 持久化事务信息
	 *
	 * @param transactionInfo 需要持久化的事务信息
	 * @return boolean 持久化是否成功
	 * @see ITransactionInfo
	 */
	protected abstract boolean persistent(ITransactionInfo transactionInfo);

	/**
	 * 当存在事务时，在完成事务前调用此方法
	 *
	 * <p>
	 * 默认实现为空，子类可重写此方法添加特定逻辑
	 * </p>
	 *
	 * @param transactionInfo 事务信息
	 * @see ITransactionInfo
	 */
	protected void completeOnhasTransaction(ITransactionInfo transactionInfo) {

	}

	/**
	 * 在提交事务时调用此方法
	 *
	 * <p>
	 * 默认实现为空，子类可重写此方法添加特定逻辑
	 * </p>
	 *
	 * @param transactionInfo 事务信息
	 * @see ITransactionInfo
	 */
	protected void commitOnTransaction(ITransactionInfo transactionInfo) {
	}

	/**
	 * 执行事务操作的内部方法，提供统一的锁机制和异常处理
	 *
	 * @param transactionId 事务ID
	 * @param doTransaction 事务执行器
	 * @return Optional<ITransactionInfo> 事务信息包装对象
	 * @see IDoTransaction 事务执行器接口
	 * @see ValueWrapper 值包装器
	 */
	private Optional<ITransactionInfo> doSomething(String transactionId, IDoTransaction doTransaction) {
		ValueWrapper<ITransactionInfo> transactionInfo = new ValueWrapper<>();
		String key = genKey(transactionId);
		LockUtil.getInstance().lockThen(getLock(key), () -> {
			doTransaction.doTransaction(key, transactionInfo);
		});
		return transactionInfo.get();
	}

	/**
	 * 开始一个新的事务或获取已存在的事务
	 *
	 * <p>
	 * 如果指定ID的事务已存在，则返回该事务；否则创建一个新的事务。
	 * </p>
	 *
	 * @param transactionId 事务ID
	 * @return ITransactionInfo 事务信息对象
	 * @throws FormattedRuntimeException 当创建或获取事务信息失败时抛出
	 * @see ITransaction#beginTransaction(String)
	 * @see #getTransactionInfo(String)
	 * @see #createTransactionInfo(String)
	 * @see #persistent(ITransactionInfo)
	 * @see #edgeControll(Optional)
	 */
	@Override
	public ITransactionInfo beginTransaction(String transactionId) {
		return doSomething(transactionId, (key, transactionInfo) -> {
			getTransactionInfo(transactionId).ifPresentOrElse(t -> {
				beginOnhasTransaction(t);
				transactionInfo.set(t);
			}, () -> {
				ITransactionInfo info = createTransactionInfo(transactionId);
				if (!TransactionStatus.BEGIN.getCode().equals(info.getTransactionStatus())) {
					log.warn("创建{}事务状态异常。将强制修正。成BEGIN", transactionId);
					info.setTransactionStatus(TransactionStatus.BEGIN);
				}
				transactionInfo.set(info);
			});
			persistent(transactionInfo.get()
					.orElseThrow(() -> new FormattedRuntimeException("{} 创建事务信息失败", transactionId)));
			edgeControll(transactionInfo.get());
		}).orElseThrow(() -> new FormattedRuntimeException("{} 获取事务信息失败", transactionId));
	}

	/**
	 * 提交事务
	 *
	 * <p>
	 * 将事务状态设置为COMMIT，并执行提交相关的操作
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象
	 * @throws FormattedRuntimeException 当事务不存在或状态异常时抛出
	 * @see ITransaction#commit(ITransactionInfo)
	 * @see TransactionStatus 事务状态枚举
	 * @see AssertUtil#isTrue(boolean, String, Object...)
	 */
	@Override
	public void commit(ITransactionInfo transactionInfo) {
		doSomething(transactionInfo.getTransactionId(), (id, r) -> {
			getTransactionInfo(id).ifPresentOrElse(t -> {
				AssertUtil.isTrue(!t.getTransactionStatus().getCode().equals(TransactionStatus.BEGIN.getCode()),
						"{} 事务状态异常。当前状态 {}", id, t.getTransactionStatus().getValue());
				t.setTransactionStatus(TransactionStatus.COMMIT);
				commitOnTransaction(t);
				persistent(t);
			}, () -> {
				AssertUtil.throwError(() -> {
					return new FormattedRuntimeException("{} 事务不存在,无法提交", id);
				});
			});
			edgeControll(r.get());
		});
	}

	/**
	 * 回滚事务
	 *
	 * <p>
	 * 将事务状态设置为ROLLBACK
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象
	 * @throws FormattedRuntimeException 当事务不存在或状态异常时抛出
	 * @see ITransaction#rollback(ITransactionInfo)
	 * @see TransactionStatus 事务状态枚举
	 * @see AssertUtil#isTrue(boolean, String, Object...)
	 */
	@Override
	public void rollback(ITransactionInfo transactionInfo) {
		doSomething(transactionInfo.getTransactionId(), (id, r) -> {
			getTransactionInfo(id).ifPresentOrElse(t -> {
				AssertUtil.isTrue(!t.getTransactionStatus().getCode().equals(TransactionStatus.BEGIN.getCode()),
						"{} 事务状态异常。当前状态 {}", id, t.getTransactionStatus().getValue());
				t.setTransactionStatus(TransactionStatus.ROLLBACK);
				persistent(t);
			}, () -> {
				AssertUtil.throwError(() -> {
					return new FormattedRuntimeException("{} 事务不存在,无法回滚", id);
				});
			});
			edgeControll(r.get());
		});
	}

	/**
	 * 完成事务
	 *
	 * <p>
	 * 将事务状态设置为COMPLETE，并执行完成相关的操作
	 * </p>
	 *
	 * @param transactionInfo 事务信息对象
	 * @throws FormattedRuntimeException 当事务不存在或状态异常时抛出
	 * @see ITransaction#complete(ITransactionInfo)
	 * @see TransactionStatus 事务状态枚举
	 * @see #completeOnhasTransaction(ITransactionInfo)
	 * @see AssertUtil#isTrue(boolean, String, Object...)
	 */
	@Override
	public void complete(ITransactionInfo transactionInfo) {
		doSomething(transactionInfo.getTransactionId(), (id, r) -> {
			getTransactionInfo(id).ifPresentOrElse(t -> {
				AssertUtil.isTrue(!t.getTransactionStatus().getCode().equals(TransactionStatus.BEGIN.getCode()),
						"{} 事务状态异常。当前状态 {}", id, t.getTransactionStatus().getValue());
				t.setTransactionStatus(TransactionStatus.COMPLETE);
				completeOnhasTransaction(t);
				persistent(t);
			}, () -> {
				AssertUtil.throwError(() -> {
					return new FormattedRuntimeException("{} 获取事务信息失败", id);
				});
			});
			edgeControll(r.get());
		});
	}

	/**
	 * 根据key获取事务信息
	 *
	 * @param key 事务key
	 * @return Optional<ITransactionInfo> 事务信息包装对象
	 * @see ITransaction#getTransaction(String)
	 * @see #getTransactionInfo(String)
	 */
	@Override
	public Optional<ITransactionInfo> getTransaction(String key) {
		return getTransactionInfo(key);
	}

	/**
	 * 事务执行器接口，用于封装事务操作逻辑
	 *
	 * @author abstergo
	 * @see AbstractTransaction#doSomething(String, IDoTransaction)
	 */
	private interface IDoTransaction {
		/**
		 * 执行事务操作
		 *
		 * @param key             事务key
		 * @param transactionInfo 事务信息值包装器，用于返回事务信息
		 * @see ValueWrapper 值包装器
		 */
		void doTransaction(String key, ValueWrapper<ITransactionInfo> transactionInfo);
	}
}
