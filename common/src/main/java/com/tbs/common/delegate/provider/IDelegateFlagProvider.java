package com.tbs.common.delegate.provider;

import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.IDelegateTarget;

/**
 * 代理标志提供者
 *
 * @author tongj
 */
public interface IDelegateFlagProvider {

    /**
     * 获取代理标志
     *
     * @param invoker
     * @param delegateTarget
     * @param <T>
     * @return
     */
    <T> IDelegateFlag<T> getDelegateFlag(IDelegateInvoker<T> invoker, IDelegateTarget<T> delegateTarget);
}
