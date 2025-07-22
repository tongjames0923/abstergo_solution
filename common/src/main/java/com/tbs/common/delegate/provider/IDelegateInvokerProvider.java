package com.tbs.common.delegate.provider;

import com.tbs.common.delegate.IDelegateInvoker;

/**
 * @author tongj
 */
public interface IDelegateInvokerProvider {

    /**
     * 创建一个调用者
     *
     * @param invokerType    调用者类型
     * @param delegateTarget 调用者目标
     * @param <T>
     * @return
     */
    <T> IDelegateInvoker<T> makeInvoker(Class<? extends IDelegateInvoker> invokerType);
}
