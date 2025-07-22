package com.tbs.common.delegate;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

import java.util.Collection;

/**
 * 执行器接口
 *
 * @author tongj
 */
public interface IDelegateInvoker<T> {

    /**
     * 设置目标对象
     *
     * @param delegateTarget
     */
    void setTarget(IDelegateTarget<T> delegateTarget);

    /**
     * 获取目标对象
     *
     * @return
     */
    IDelegateTarget<T> getTarget();

    /**
     * 代理设置目标对象属性值
     *
     * @param method
     * @param p
     * @param <P>
     * @return
     */
    <P> IDelegateInvoker<T> takeIn(SBiConsumer<T, P> method, SSupplier<P> p);

    /**
     * 代理读取目标特定的属性值
     *
     * @param method
     * @param consumer
     * @param <P>
     * @return
     */
    <P> IDelegateInvoker<T> takeOut(SFunction<T, P> method, SConsumer<P> consumer);

    /**
     * 执行代理
     *
     * @return
     */
    IDelegateFlag<T> executeOnce();

    /**
     * 执行代理
     *
     * @return
     */
    Collection<IDelegateFlag<T>> executeAll();

    /**
     * 等待执行完成的个数
     *
     * @return
     */
    int waittingCount();

}
