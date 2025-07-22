package com.tbs.common.delegate.impl;

import com.tbs.common.delegate.AbstractDelegate;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SFunction;

/**
 * 简单的delegate实现
 *
 * @param <T>
 * @author abstergo
 */
public class MainThreadCallDelegate<T> extends AbstractDelegate<T> {
    /**
     * 构造委托
     *
     * @param target
     */
    public MainThreadCallDelegate(T target) {
        super(target);
    }

    @Override
    public <P> void delegate(SBiConsumer<T, P> method, P param) {
        method.accept(getTarget(), param);
    }

    @Override
    public <R> R delegateAndReturn(SFunction<T, R> method) {
        return method.apply(getTarget());
    }
}
