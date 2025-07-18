package com.tbs.sql.order;

import com.tbs.sql.enums.QueryOrderEnum;

import java.lang.reflect.Field;

/**
 *
 * @author tongj
 */
public interface IQueryOrderBy {
    QueryOrderEnum orderBy(Object target, Field field, String mappedName);
}
