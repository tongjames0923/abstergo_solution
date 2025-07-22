package com.tbs.async.delegate;

import com.tbs.common.delegate.AbstractDelegateInvoker;
import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

import java.util.concurrent.ExecutorService;

/**
 * 异步执行代理调用
 *
 * @author tongj
 */
public class AsyncDelegateInvoker<T> extends AbstractDelegateInvoker<T> {
    /**
     * 线程池
     */
    private ExecutorService executorService;

    private IDelegateFlagProvider delegateFlagProvider;

    public AsyncDelegateInvoker(IDelegateFlagProvider delegateFlagProvider, ExecutorService executorService) {
        this.executorService = executorService;
        this.delegateFlagProvider = delegateFlagProvider;
    }

    @Override
    protected IDelegateFlagProvider getDelegateFlagProvider() {
        return delegateFlagProvider;
    }

    @Override
    protected <P> void executeTakeIn(IDelegateFlag<T> flag, SBiConsumer<T, P> method, SSupplier<P> p) {
        executorService.execute(() -> {
            getTarget().write(method, p);
            flag.setFinished(this);
        });
    }

    @Override
    protected <P> void executeTakeOut(IDelegateFlag<T> flag, SFunction<T, P> method, SConsumer<P> consumer) {
        executorService.execute(() -> {
            getTarget().read(method, consumer);
            flag.setFinished(this);
        });
    }
}
