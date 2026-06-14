package com.tbs.sql.order.impl;

import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.IQueryOrderBy;

import java.lang.reflect.Field;

public final class DescStaticOrder implements IQueryOrderBy {
    @Override
    public OrderEnum orderBy(Object target, Field field, String mappedName) {
        return OrderEnum.DESC;
    }
}
