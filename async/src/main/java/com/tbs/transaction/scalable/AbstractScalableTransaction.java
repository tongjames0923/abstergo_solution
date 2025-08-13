package com.tbs.transaction.scalable;

import java.util.Optional;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.lock.ILock;
import com.tbs.lock.ILockProvider;
import com.tbs.transaction.AbstractTransaction;
import com.tbs.transaction.ITransactionInfo;

/**
 * 可扩展的事务抽象类，提供事务处理的基本框架和分布式锁机制
 * <p>
 * 该类定义了事务处理的核心方法，包括事务的创建、存储、获取和持久化等操作。 子类需要实现抽象方法以提供具体的锁提供者、事务存储和事务创建器实现。
 * </p>
 */
public abstract class AbstractScalableTransaction extends AbstractTransaction {

	/**
	 * 获取锁提供者实例
	 *
	 * @return ILockProvider 锁提供者，用于获取分布式锁实例
	 * @see ILockProvider 分布式锁提供者接口
	 * @see com.tbs.lock.ILock 分布式锁接口
	 */
	protected abstract ILockProvider getLockProvider();

	/**
	 * 获取事务存储实例
	 *
	 * @return ITransactionStore 事务存储器，用于存储和获取事务信息
	 * @see ITransactionStore 事务存储接口
	 * @see ITransactionStore#store(String, ITransactionInfo) 存储事务信息方法
	 * @see ITransactionStore#get(String) 获取事务信息方法
	 */
	protected abstract ITransactionStore getTransactionStore();

	/**
	 * 获取事务创建器实例
	 *
	 * @return ITransactionCreator 事务创建器，用于创建新的事务信息对象
	 * @see ITransactionCreator 事务创建器接口
	 * @see ITransactionCreator#createTransactionInfo(String) 创建事务信息方法
	 */
	protected abstract ITransactionCreator getTransactionCreator();

	/**
	 * 获取事务状态改变监听器实例
	 *
	 * @return ITransactionStatusChangeLisenner 事务状态改变监听器的Optional包装对象，
	 *         如果存在监听器则在事务状态变更时触发相应事件
	 * @see ITransactionStatusChangeLisenner 事务状态变更监听器接口
	 * @see ITransactionStatusChangeLisenner#onCommit(ITransactionInfo) 事务提交事件
	 * @see ITransactionStatusChangeLisenner#onRollback(ITransactionInfo) 事务回滚事件
	 * @see ITransactionStatusChangeLisenner#onComplete(ITransactionInfo) 事务完成事件
	 */
	protected abstract Optional<ITransactionStatusChangeLisenner> getTransactionStatusChangeLisenner();

	/**
	 * 获取事务冲突解决器实例
	 *
	 * @return ITransactionConflictSolver 事务冲突解决器的Optional包装对象，
	 *         如果存在解决器则在创建事务于已存在事务上时触发冲突解决逻辑
	 * @see ITransactionConflictSolver 事务冲突解决器接口
	 * @see ITransactionConflictSolver#onCreateTransactionOnExistingTransaction(ITransactionInfo)
	 *      在已存在事务上创建新事务时的冲突处理方法
	 */
	protected abstract Optional<ITransactionConflictSolver> getTransactionConflictSolver();

	/**
	 * 获取事务边界控制器实例
	 *
	 * @return ITransactionEdgeController 事务边界控制器的Optional包装对象，
	 *         如果存在控制器则在事务状态变更时触发边界控制逻辑
	 * @see ITransactionEdgeController 事务边界控制器接口
	 * @see ITransactionEdgeController#onCreateEdge(ITransactionInfo) 事务创建边界控制
	 * @see ITransactionEdgeController#onCommitEdge(ITransactionInfo) 事务提交边界控制
	 * @see ITransactionEdgeController#onRollbackEdge(ITransactionInfo) 事务回滚边界控制
	 * @see ITransactionEdgeController#onCompleteEdge(ITransactionInfo) 事务完成边界控制
	 */
	protected abstract Optional<ITransactionEdgeController> getTransactionEdgeController();

	@Override
	protected ILock getLock(String key) {
		return getLockProvider().getLocker(key);
	}

	@Override
	protected String genKey(String transactionId) {
		return "transaction-key::" + EncryptUitl.hash128Encrypt(transactionId, "transaction-key-salt") + ".transaction";
	}

	@Override
	protected Optional<ITransactionInfo> getTransactionInfo(String transactionId) {
		return getTransactionStore().get(transactionId);
	}

	@Override
	protected ITransactionInfo createTransactionInfo(String transactionId) {
		return getTransactionCreator().createTransactionInfo(transactionId);
	}

	@Override
	protected boolean persistent(ITransactionInfo transactionInfo) {
		return getTransactionStore().store(transactionInfo.getTransactionId(), transactionInfo);
	}

	@Override
	protected void beginOnhasTransaction(ITransactionInfo transactionInfo) {
		super.beginOnhasTransaction(transactionInfo);
		getTransactionConflictSolver()
				.ifPresent(solver -> solver.onCreateTransactionOnExistingTransaction(transactionInfo));
	}

	@Override
	protected void completeOnhasTransaction(ITransactionInfo transactionInfo) {
		super.completeOnhasTransaction(transactionInfo);
		getTransactionStatusChangeLisenner().ifPresent(lisenner -> lisenner.onComplete(transactionInfo));
	}

	@Override
	protected void commitOnTransaction(ITransactionInfo transactionInfo) {
		super.commitOnTransaction(transactionInfo);
		getTransactionStatusChangeLisenner().ifPresent(lisenner -> lisenner.onCommit(transactionInfo));
	}

	@Override
	protected void edgeControll(Optional<ITransactionInfo> transactionInfo) {
		getTransactionEdgeController().ifPresent(controller -> {
			transactionInfo.ifPresent((t) -> {
				switch (t.getTransactionStatus()) {
				case COMMIT:
					controller.onCommitEdge(t);
					break;
				case ROLLBACK:
					controller.onRollbackEdge(t);
					break;
				case COMPLETE:
					controller.onCompleteEdge(t);
					break;

				case BEGIN:
					controller.onCreateEdge(t);
					break;
				default:
					AssertUtil.throwError(() -> new IllegalArgumentException("未定义的TransactionStatus"));
					break;
				}
			});
		});

	}
}
