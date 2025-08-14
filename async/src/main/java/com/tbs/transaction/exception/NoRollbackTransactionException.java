package com.tbs.transaction.exception;

import com.tbs.common.exception.FormattedRuntimeException;

/**
 * 不执行回滚的事务异常
 */
public class NoRollbackTransactionException extends FormattedRuntimeException {
	public <T extends Throwable> NoRollbackTransactionException(T cause, String message, Object... params) {
		super(cause, message, params);
	}
}
