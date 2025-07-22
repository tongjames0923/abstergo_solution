package com.tbs.common.delegate;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SFunction;

/**
 * 委托执行接口
 *
 * @author abstergo
 */
public interface IDelegate<T> {
    /**
     * 获取委托对象
     *
     * @return
     */
    T getTarget();

    /**
     * 重置委托对象
     *
     * @param target
     */
    void reset(T target);

    /**
     * 委托执行
     *
     * @param method
     * @param param
     */
    <P> void delegate(SBiConsumer<T, P> method, P param);

    /**
     * 委托执行并返回结果
     *
     * @param method
     * @return
     */
    <R> R delegateAndReturn(SFunction<T, R> method);

}
