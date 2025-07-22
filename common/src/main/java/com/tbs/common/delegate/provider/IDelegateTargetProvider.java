package com.tbs.common.delegate.provider;

import com.tbs.common.delegate.IDelegateTarget;

/**
 * 代理对象提供者
 *
 * @author tbs
 */
public interface IDelegateTargetProvider {
    /**
     * 获取代理对象
     *
     * @param targetObject
     * @return
     */
    <T> IDelegateTarget<T> getDelegateTarget(T targetObject);
}
