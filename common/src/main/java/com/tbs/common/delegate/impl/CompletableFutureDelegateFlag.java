package com.tbs.common.delegate.impl;

import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.exception.FormattedRuntimeException;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFutureDelegateFlag
 *
 * @param <T>
 * @author tongj
 */
public class CompletableFutureDelegateFlag<T> implements IDelegateFlag<T> {

    private CompletableFuture<IDelegateTarget<T>> future;

    private IDelegateTarget<T> delegateTarget;
    private IDelegateInvoker<T> invoker;

    public CompletableFutureDelegateFlag(IDelegateTarget<T> delegateTarget, IDelegateInvoker<T> invoker) {
        this.delegateTarget = delegateTarget;
        this.invoker = invoker;
        future = new CompletableFuture<>();
    }

    @Override
    public IDelegateTarget<T> getUnitlFinished() {
        return future.join();
    }

    @Override
    public void setFinished(IDelegateInvoker<T> invoker) {
        if (invoker == this.invoker) {
            future.complete(delegateTarget);
        } else {
            future.completeExceptionally(new FormattedRuntimeException("非相同调用器设置"));
        }
    }

    @Override
    public boolean isFinished() {
        return future.isDone();
    }
}
