package com.tbs.common.data;

/**
 * 功能包装接口
 *
 * @param <T>
 * @author abstergo
 */
public interface IFeatureWrapper<T> {
    /**
     * 获取包装对象
     *
     * @return
     */
    T getOrginal();

    /**
     * 设置包装对象
     *
     * @param orginal
     */
    void setOrginal(T orginal);
}
