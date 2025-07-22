package com.tbs.common.delegate;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

/**
 * 对象代理目标
 *
 * @param <T>
 * @author tongj
 */
public interface IDelegateTarget<T> {

    /**
     * 设置对象
     *
     * @param targetObject
     */
    void setTarget(T targetObject);

    /**
     * 读取对象
     *
     * @param method   读取的方法
     * @param consumer 消费读取出来的值
     * @param <P>
     */
    <P> void read(SFunction<T, P> method, SConsumer<P> consumer);

    /**
     * 写入对象
     *
     * @param setter 写入的方法
     * @param val    写入的值
     * @param <P>
     */
    <P> void write(SBiConsumer<T, P> setter, SSupplier<P> val);

    /**
     * 获取对象
     *
     * @return
     */
    T getTarget();
}
