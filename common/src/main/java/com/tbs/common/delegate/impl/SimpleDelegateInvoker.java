package com.tbs.common.delegate.impl;

import com.tbs.common.delegate.AbstractDelegateInvoker;
import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.IDelegateFlagProvider;
import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

/**
 * 简单的delegate执行器
 *
 * @author tongj
 */
public class SimpleDelegateInvoker<T> extends AbstractDelegateInvoker<T> {

    public SimpleDelegateInvoker(IDelegateTarget<T> delegateTarget, IDelegateFlagProvider delegateFlagProvider) {
        super(delegateTarget, delegateFlagProvider);
    }

    @Override
    protected <P> void executeTakeIn(IDelegateFlag<T> flag, SBiConsumer<T, P> method, SSupplier<P> p) {
        getTarget().write(method, p);
        flag.setFinished(this);
    }

    @Override
    protected <P> void executeTakeOut(IDelegateFlag<T> flag, SFunction<T, P> method, SConsumer<P> consumer) {
        getTarget().read(method, consumer);
        flag.setFinished(this);
    }
}
