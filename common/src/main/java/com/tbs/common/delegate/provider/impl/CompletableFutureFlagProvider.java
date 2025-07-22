package com.tbs.common.delegate.provider.impl;

import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.impl.CompletableFutureDelegateFlag;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.IDelegateTarget;

/**
 * 默认的CompletableFutureDelegateFlag提供者
 *
 * @author tongj
 */
public class CompletableFutureFlagProvider implements IDelegateFlagProvider {
    @Override
    public <T> IDelegateFlag<T> getDelegateFlag(IDelegateInvoker<T> invoker, IDelegateTarget<T> delegateTarget) {
        return new CompletableFutureDelegateFlag<>(delegateTarget, invoker);
    }
}
