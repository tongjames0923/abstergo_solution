package com.tbs.common.delegate.impl;

import com.tbs.common.delegate.*;
import com.tbs.common.delegate.provider.IDelegateInvokerProvider;
import com.tbs.common.delegate.provider.IDelegateTargetProvider;

import javax.annotation.Resource;

/**
 * Spring托管的代理模板
 *
 * @author tongj
 */
public class SpringManagedDelegateTemplate implements IDelegateTemplate {

    @Resource
    private IDelegateTargetProvider provider;

    @Resource
    private IDelegateInvokerProvider invokerProvider;

    @Override
    public <T> IDelegateTarget<T> createDelegateTarget(T targetObject) {
        return provider.getDelegateTarget(targetObject);
    }

    @Override
    public <T> IDelegateInvoker<T> createDelegateInvoker(Class<? extends IDelegateInvoker> invokerType,
        IDelegateTarget<T> delegateTarget) {
        IDelegateInvoker<T> invoker = invokerProvider.makeInvoker(invokerType);
        invoker.setTarget(delegateTarget);
        return invoker;
    }
}
