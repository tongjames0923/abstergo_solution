package com.tbs.common.data;

import org.jetbrains.annotations.NotNull;

/**
 * 针对不可序列化的对象进行序列化
 *
 * @author tongj
 */
public interface ISerializer {
    /**
     * 是否支持该对象进行序列化
     *
     * @param obj
     * @return
     */
    boolean support(@NotNull Object obj);

    /**
     * 序列化对象
     *
     * @param obj
     * @param clazz
     * @return
     */
    <T> byte[] serialize(@NotNull T obj, @NotNull Class<T> clazz);

    /**
     * 反序列化对象
     *
     * @param bytes
     * @param clazz
     * @return
     */
    <T> T deserializeAs(@NotNull byte[] bytes, @NotNull Class<T> clazz);
}
