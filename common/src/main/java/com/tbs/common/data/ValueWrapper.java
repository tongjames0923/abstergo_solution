package com.tbs.common.data;

import java.util.Optional;

/**
 * 值包装类
 *
 * @author abstergo
 */
public class ValueWrapper<T> {
    private T value;

    /**
     * 创建一个值包装类
     *
     * @param value
     * @param <T>
     * @return
     */
    public static <T> ValueWrapper<T> of(T value) {
        ValueWrapper<T> wrapper = new ValueWrapper<>();
        wrapper.set(value);
        return wrapper;
    }

    /**
     * 获取值
     *
     * @return
     */
    public Optional<T> get() {
        return Optional.ofNullable(value);
    }

    /**
     * 获取值，如果为空则抛出异常
     *
     * @return
     */
    public T getOrThrow() {
        return get().orElseThrow(() -> new NullPointerException("值包装器异常。空值获取"));
    }

    /**
     * 设置值
     *
     * @param value
     */
    public void set(T value) {
        this.value = value;
    }

}
