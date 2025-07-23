package com.tbs.common;

/**
 * 枚举基类
 *
 * @author tongj
 */
public interface BaseEnum<T> {

    /**
     * 获取枚举详情值
     *
     * @return
     */
    public T getValue();

    /**
     * 获取枚举值
     *
     * @return
     */
    public Integer getCode();

}
