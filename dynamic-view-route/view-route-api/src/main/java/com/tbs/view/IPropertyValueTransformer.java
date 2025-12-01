package com.tbs.view;

import com.tbs.common.data.ITransformer;

import java.nio.charset.StandardCharsets;

/**
 * 属性值转换器
 *
 * @author jiangnan
 * @date 2020/05/07
 */
public interface IPropertyValueTransformer extends ITransformer<Object> {

    /**
     * 转换为字符串
     *
     * @param value
     * @return
     */
    default String transformStr(Object value) {
        return new String(transform(value), StandardCharsets.UTF_8);
    }

    /**
     * 反转字符串
     *
     * @param value
     * @param clazz
     * @return
     */
    default <T> T reverseTransformStr(String value, Class<T> clazz) {
        return reverseTransform(value.getBytes(StandardCharsets.UTF_8), clazz);
    }

}
