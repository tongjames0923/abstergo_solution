package com.tbs.common.delegate;

/**
 * 代理模板
 *
 * @author tongj
 */
public interface IDelegateTemplate {

    /**
     * 创建代理目标对象
     *
     * @param targetObject
     * @param <T>
     * @return
     */
    <T> IDelegateTarget<T> createDelegateTarget(T targetObject);

    /**
     * 创建代理调用者
     *
     * @param invokerType
     * @param delegateTarget
     * @param <T>
     * @return
     */
    <T> IDelegateInvoker<T> createDelegateInvoker(Class<? extends IDelegateInvoker> invokerType,
        IDelegateTarget<T> delegateTarget);

}
