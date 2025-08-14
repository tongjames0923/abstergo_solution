package com.tbs.broadcast.idempotency;

import org.jetbrains.annotations.NotNull;

import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.transaction.ITransaction;
import com.tbs.transaction.ITransactionInfo;
import com.tbs.transaction.exception.NoRollbackTransactionException;

/**
 * @author abstergo
 */
public abstract class AbstractEventIdempotencyManager implements IEventIdempotencyManager {

	private static void runWithTransaction(ITransaction transaction, String key, Runnable runnable) {
		ITransactionInfo transactionInfo = null;
		try {
			transactionInfo = transaction.beginTransaction(key);
		} catch (RuntimeException e) {
			AssertUtil.throwError(() -> e);
			return;
		}
		if (transactionInfo == null) {
			AssertUtil.throwError(() -> new FormattedRuntimeException("事务{}不存在", key));
		}
		try {
			runnable.run();
			transaction.commit(transactionInfo);
		} catch (NoRollbackTransactionException ex) {
			AssertUtil.throwError(() -> ex);
		} catch (RuntimeException e) {
			transaction.rollback(transactionInfo);
			AssertUtil.throwError(() -> e);
		} finally {
			transaction.complete(transactionInfo);
		}

	}

	/**
	 * 生成key
	 *
	 * @param publishResult
	 * @return
	 */
	protected abstract String genKey(@NotNull IPublishResult publishResult);

	/**
	 * 获取事务
	 *
	 * @return
	 */
	protected abstract ITransaction getTransaction();

	/**
	 * 获取幂等性结果
	 *
	 * @param key
	 * @return
	 */
	protected abstract EventIdempotencyResult getIdempotencyResult(String key);

	/**
	 * 设置幂等性结果
	 *
	 * @param key
	 * @param result
	 */
	protected abstract void setIdempotencyResult(String key, EventIdempotencyResult result);

	@Override
	public @NotNull EventIdempotencyResult checkIdempotency(@NotNull IPublishResult publishResult) {
		String key = genKey(publishResult);
		ITransaction transaction = getTransaction();
		ValueWrapper<EventIdempotencyResult> result = new ValueWrapper<>();
		runWithTransaction(transaction, key, () -> {
			result.set(getIdempotencyResult(key));
		});

		return result.get().orElseThrow(() -> {
			return new FormattedRuntimeException("幂等性检查失败 key={}", key);
		});
	}

	@Override
	public void submitIdempotencyCheck(@NotNull IPublishResult publishResult, @NotNull EventIdempotencyResult result) {
		String key = genKey(publishResult);
		runWithTransaction(getTransaction(), key, () -> {
			setIdempotencyResult(key, result);
		});
	}
}
