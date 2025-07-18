package com.tbs.sql.provider.impl;

import com.tbs.sql.provider.IAutoValueProvider;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * DateValueAutoProvider 是一个实现 IAutoValueProvider 接口的类，用于自动提供 Date 和 LocalDateTime 类型的值。
 * </p>
 *
 * <p>
 * 该类实现了 IAutoValueProvider 接口，并重写了 getValue 和 support 方法。
 * </p>
 *
 * @author abstergo
 */
public class DateValueAutoProvider implements IAutoValueProvider {

    /**
     * 根据目标对象和字段类型，返回相应的值。
     *
     * @param target 目标对象
     * @param field  字段
     * @return 如果字段类型是 Date 或 LocalDateTime，则返回相应的实例；否则返回 null
     */
    @Override
    public Object getValue(Object target, Field field) {
        if (field.getType().equals(Date.class)) {
            return new Date();
        } else if (field.getType().equals(LocalDateTime.class)) {
            return LocalDateTime.now();
        } else {
            return null;
        }
    }

    /**
     * 判断是否支持为指定类型和旧值提供自动值。
     *
     * @param type     字段类型
     * @param oldValue 旧值
     * @return 如果字段类型是 Date 或 LocalDateTime 并且旧值为 null，则返回 true；否则返回 false
     */
    @Override
    public boolean support(Class<?> type, Object oldValue) {
        return (Date.class.equals(type) || LocalDateTime.class.equals(type)) && oldValue == null;
    }
}
