package com.tbs.sql.order;

import com.tbs.sql.enums.OrderEnum;

import java.lang.reflect.Field;

/**
 * 排序策略接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IQueryOrderBy {
    OrderEnum orderBy(Object target, Field field, String mappedName);
}
