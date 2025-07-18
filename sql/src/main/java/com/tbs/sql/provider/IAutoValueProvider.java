package com.tbs.sql.provider;

import java.lang.reflect.Field;

/**
 * @author abstergo
 */
public interface IAutoValueProvider {
    /**
     * 根据目标对象和字段类型，返回相应的值。
     *
     * @param target 目标对象
     * @param field  字段
     * @return
     */
    Object getValue(Object target, Field field);

    /**
     * 判断是否支持为指定类型和旧值提供自动值。
     *
     * @param type     字段类型
     * @param oldValue 旧值
     */
    boolean support(Class<?> type, Object oldValue);
}
