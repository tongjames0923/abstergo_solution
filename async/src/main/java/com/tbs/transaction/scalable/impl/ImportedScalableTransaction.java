package com.tbs.transaction.scalable.impl;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.tbs.common.error.AssertUtil;
import com.tbs.lock.ILockProvider;
import com.tbs.transaction.scalable.*;

import lombok.Setter;

public class ImportedScalableTransaction extends AbstractScalableTransaction {

	private ILockProvider lockProvider;
	private ITransactionCreator creator;
	private ITransactionStore store;
	@Setter
	private ITransactionStatusChangeLisenner lisenner;
	@Setter
	private ITransactionConflictSolver conflictSolver;
	@Setter
	private ITransactionEdgeController edgeController;

	public ImportedScalableTransaction(@NotNull ILockProvider lockProvider, @NotNull ITransactionCreator creator,
			@NotNull ITransactionStore store) {
		AssertUtil.notNull(lockProvider, "锁提供器不能为空");
		AssertUtil.notNull(creator, "事务创建器不能为空");
		AssertUtil.notNull(store, "事务储存器不能为空");
		this.lockProvider = lockProvider;
		this.creator = creator;
		this.store = store;
	}

	@Override
	protected ILockProvider getLockProvider() {
		return lockProvider;
	}

	@Override
	protected ITransactionStore getTransactionStore() {
		return store;
	}

	@Override
	protected ITransactionCreator getTransactionCreator() {
		return creator;
	}

	@Override
	protected Optional<ITransactionStatusChangeLisenner> getTransactionStatusChangeLisenner() {
		return Optional.ofNullable(lisenner);
	}

	@Override
	protected Optional<ITransactionConflictSolver> getTransactionConflictSolver() {
		return Optional.ofNullable(conflictSolver);
	}

	@Override
	protected Optional<ITransactionEdgeController> getTransactionEdgeController() {
		return Optional.ofNullable(edgeController);
	}
}
