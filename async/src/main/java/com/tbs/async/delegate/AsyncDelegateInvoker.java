package com.tbs.async.delegate;

import com.tbs.common.delegate.AbstractDelegateInvoker;
import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;
import com.tbs.thread.IOnThreadError;
import com.tbs.thread.IThreadPoolManager;
import com.tbs.thread.impl.SimpleThreadRequest;

import jakarta.annotation.Resource;
import java.util.concurrent.ExecutorService;

/**
 * 异步执行代理调用
 *
 * @author tongj
 */
public class AsyncDelegateInvoker<T> extends AbstractDelegateInvoker<T> {

    private IDelegateFlagProvider delegateFlagProvider;

    @Resource
    IThreadPoolManager threadPoolManager;

    public AsyncDelegateInvoker(IDelegateFlagProvider delegateFlagProvider) {
        this.delegateFlagProvider = delegateFlagProvider;
    }

    @Override
    protected IDelegateFlagProvider getDelegateFlagProvider() {
        return delegateFlagProvider;
    }

    @Override
    protected <P> void executeTakeIn(IDelegateFlag<T> flag, SBiConsumer<T, P> method, SSupplier<P> p) {

        threadPoolManager.submit(SimpleThreadRequest.Builder.create("asyncDelegateInvoker-task in", () -> {
            getTarget().write(method, p);
            flag.setFinished(this);
        }).build(), new IOnThreadError() {
            @Override
            public void onThreadError(Throwable throwable, Thread at) {
                onErrorThrow(flag, throwable);
            }
        });

    }

    @Override
    protected <P> void executeTakeOut(IDelegateFlag<T> flag, SFunction<T, P> method, SConsumer<P> consumer) {
        threadPoolManager.submit(SimpleThreadRequest.Builder.create("asyncDelegateInvoker-task in", () -> {
            getTarget().read(method, consumer);
            flag.setFinished(this);
        }).build(), new IOnThreadError() {
            @Override
            public void onThreadError(Throwable throwable, Thread at) {
                onErrorThrow(flag, throwable);
            }
        });
    }
}
