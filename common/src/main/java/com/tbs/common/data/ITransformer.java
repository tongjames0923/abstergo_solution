package com.tbs.common.data;

/**
 * 数据转换接口
 *
 * @author abstergo
 */
public interface ITransformer<V> {
    /**
     * 转换
     *
     * @param value
     * @return
     */
    byte[] transform(V value);

    /**
     * 反向转换
     *
     * @param value
     * @param clazz
     * @return
     */
    <T extends V> T reverseTransform(byte[] value, Class<T> clazz);
}
