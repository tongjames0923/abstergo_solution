package com.tbs.sql.converter;

/**
 * 值转换器接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IValueConverter {
    boolean support(Class<?> type, Object oldValue);
    String convert(Class<?> type, Object oldValue);
    int getPriority();
}
