package com.tbs.common.delegate;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SSupplier;

/**
 * 代理方法执行器
 *
 * @author abstergo
 */
public class DelegateMethodExecutor<T, P, M extends SBiConsumer<T, P>> {
    private IDelegate<T> delegate;

    private M method;

    private SSupplier<P> supplier;

    public DelegateMethodExecutor(IDelegate<T> delegate, M method, SSupplier<P> supplier) {
        this.delegate = delegate;
        this.method = method;
        this.supplier = supplier;
    }

    public T execute() {
        P param = delegate.delegateAndReturn((v) -> {
            return supplier.get();
        });
        delegate.delegate(method, param);
        return delegate.getTarget();
    }

}
