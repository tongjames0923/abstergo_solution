package com.tbs.shared.interfaces.view;

/**
 * 属性值转换器
 *
 * @author jiangnan
 * @date 2020/05/07
 */
public interface IPropertyValueTransformer {

    /**
     * 转换
     *
     * @param value
     * @return
     */
    String transform(Object value);

    /**
     * 反向转换
     *
     * @param value
     * @return
     */
    <T> T reverseTransform(String value, Class<T> clazz);

}
