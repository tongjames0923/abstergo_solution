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

    private Throwable exception = null;

    public CompletableFutureDelegateFlag(IDelegateTarget<T> delegateTarget, IDelegateInvoker<T> invoker) {
        this.delegateTarget = delegateTarget;
        this.invoker = invoker;
        future = new CompletableFuture<>();
    }

    @Override
    public IDelegateTarget<T> get() {
        return delegateTarget;
    }

    @Override
    public IDelegateFlag<T> waitUntilFinished() {
        future.join();
        return this;
    }

    @Override
    public boolean setFinished(IDelegateInvoker<T> invoker) {
        if (invoker == this.invoker) {
            future.complete(delegateTarget);
            return true;
        }
        return false;
    }

    @Override
    public boolean setException(Throwable e, IDelegateInvoker<T> invoker) {
        if (invoker == this.invoker) {
            future.completeExceptionally(e);
            this.exception = e;
            return true;
        }
        return false;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public boolean isFinished() {
        return future.isDone();
    }
}
