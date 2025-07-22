package com.tbs.common.delegate.impl;

import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

/**
 * 默认的调用者运行委托目标
 *
 * @author tongj
 */
public class CallerRunsDelegateTarget<T> implements IDelegateTarget<T> {

    private T targetObject;

    public CallerRunsDelegateTarget(T targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public void setTarget(T targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public <P> void read(SFunction<T, P> method, SConsumer<P> consumer) {
        consumer.accept(method.apply(targetObject));
    }

    @Override
    public <P> void write(SBiConsumer<T, P> setter, SSupplier<P> val) {
        setter.accept(targetObject, val.get());
    }

    @Override
    public T getTarget() {
        return targetObject;
    }
}
