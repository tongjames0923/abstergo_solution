package com.tbs.sql.provider.impl;

import com.tbs.sql.provider.IAutoValueProvider;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 日期自动值提供者
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DateValueAutoProvider implements IAutoValueProvider {
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

    @Override
    public boolean support(Class<?> type, Object oldValue) {
        return (Date.class.equals(type) || LocalDateTime.class.equals(type)) && oldValue == null;
    }
}
