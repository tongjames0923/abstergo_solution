package com.tbs.transaction.scalable.conflict;

import com.tbs.common.error.AssertUtil;
import com.tbs.transaction.ITransactionInfo;
import com.tbs.transaction.exception.NoRollbackTransactionException;
import com.tbs.transaction.scalable.ITransactionConflictSolver;

/**
 * 唯一事务冲突解决器
 *
 *
 **/
public class UniqueTransactionConflictSolver implements ITransactionConflictSolver {
	@Override
	public void onCreateTransactionOnExistingTransaction(ITransactionInfo transactionInfo) {
		AssertUtil.throwError(() -> new NoRollbackTransactionException(null, "已存在事务，且事务独占"));
	}
}
