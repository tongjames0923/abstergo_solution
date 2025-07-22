package com.tbs.common.delegate;

import com.tbs.common.function.SSupplier;

/**
 * 代理提供者
 *
 * @author abstergo
 */
public interface IDelegateProvider {

    /**
     * 获取代理对象
     *
     * @param val
     * @param <T>
     * @return
     */
    <T> IDelegate<T> getDelegate(T val);

    /**
     * 获取代理对象
     *
     * @param supplier
     * @param <T>
     * @return
     */
    default <T> IDelegate<T> getDelegate(SSupplier<T> supplier) {
        return getDelegate(supplier.get());
    }
}
