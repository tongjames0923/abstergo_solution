package com.tbs.sql.order.impl;

import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.order.IQueryOrderBy;
import com.tbs.sql.order.IQueryOrderedModel;

import java.lang.reflect.Field;

/**
 * @author tongj
 */
public class OrderedModelOrderImpl implements IQueryOrderBy {
    @Override
    public QueryOrderEnum orderBy(Object target, Field field, String mappedName) {
        QueryOrderEnum result = null;
        if (IQueryOrderedModel.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            try {
                IQueryOrderedModel orderedModel = (IQueryOrderedModel)field.get(target);
                result = orderedModel.getOrdered();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                field.setAccessible(false);
            }
        } else {
            throw new UnsupportedOperationException("only support for IOrderedModel");
        }

        return result;
    }
}
