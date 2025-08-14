package com.tbs.transaction.scalable.info.impl;

import com.tbs.transaction.ITransactionInfo;
import com.tbs.transaction.scalable.ITransactionCreator;

/**
 * 默认事务创建者
 *
 * @author jiangnan
 *
 */
public class DefaultTransactionCreator implements ITransactionCreator {
	@Override
	public ITransactionInfo createTransactionInfo(String transactionId) {
		return new DefaultRollbackableTransactionInfo(transactionId);
	}
}
