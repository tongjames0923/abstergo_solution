package com.tbs.common.holder.context;

import java.io.Serializable;

/**
 * 上下文数据接口
 *
 * @author abstergo
 */
public interface IContext extends Serializable {
    /**
     * 获取上下文ID
     *
     * @return
     */
    String getContextId();

    /**
     * 获取上下文数据是否有效
     *
     * @return
     */
    boolean isValid();

    /**
     * 重建上下文，使上下文重新生效
     *
     * @return
     */
    IContext rebuildContext();

    /**
     * 外部提示该上下文可能失效
     */
    void markInvalid();

    /**
     * 获取上下文数据
     *
     * @param key
     * @return
     */
    Object get(String key);

    /**
     * 获取上下文数据
     *
     * @param key
     * @param clazz
     * @return
     */
    default <T> T get(String key, Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    /**
     * 设置上下文数据
     *
     * @param key
     * @param value
     */
    void set(String key, Object value);

    /**
     * 移除上下文数据
     *
     * @param key
     */
    void remove(String key);
}
