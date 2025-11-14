package com.tbs.sql.order.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
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
                if (orderedModel == null) {
                    return null;
                }
                result = orderedModel.getOrdered();
            } catch (Exception e) {
                AssertUtil.throwError(() -> new FormattedRuntimeException(e, "获取OrderedModel失败"));
            } finally {
                field.setAccessible(false);
            }
        } else {
            AssertUtil.throwError(
                () -> new FormattedRuntimeException("OrderedModelOrderImpl只支持IQueryOrderedModel").errorLevel(
                    ErrorLevelEnum.CRITICAL_SYSTEM_ERROR));
        }

        return result;
    }
}
