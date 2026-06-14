package com.tbs.sql.provider;

import java.lang.reflect.Field;

/**
 * 自动值提供者接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IAutoValueProvider {
    Object getValue(Object target, Field field);
    boolean support(Class<?> type, Object oldValue);
}
