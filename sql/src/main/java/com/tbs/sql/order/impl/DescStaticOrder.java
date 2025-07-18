package com.tbs.sql.order.impl;

import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.order.IQueryOrderBy;

import java.lang.reflect.Field;

public final class DescStaticOrder implements IQueryOrderBy {

    public static final IQueryOrderBy DESC = null;

    @Override
    public QueryOrderEnum orderBy(Object target, Field field, String mappedName) {
        return QueryOrderEnum.DESC;
    }
}
