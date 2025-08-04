package com.tbs.common.data;

import org.jetbrains.annotations.NotNull;

/**
 * 针对不可序列化的对象进行序列化
 *
 * @author tongj
 */
public interface IDataBridge<T, E> {
    /**
     * 桥接
     *
     * @param obj
     * @return
     */
    T bridge(@NotNull E obj);

    /**
     * 反桥接
     *
     * @param obj
     * @return
     */
    E unbridge(@NotNull T obj);
}
