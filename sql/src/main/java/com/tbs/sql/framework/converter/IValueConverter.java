package com.tbs.sql.framework.converter;

/**
 * 值转换器
 *
 * @author abstergo
 */
public interface IValueConverter {

    /**
     * 是否支持转换
     *
     * @param type
     * @param oldValue
     * @return
     */
    boolean support(Class<?> type, Object oldValue);

    /**
     * 转换
     *
     * @param type
     * @param oldValue
     * @return
     */
    String convert(Class<?> type, Object oldValue);

    /**
     * 获取优先级
     *
     * @return
     */
    int getPriority();
}
