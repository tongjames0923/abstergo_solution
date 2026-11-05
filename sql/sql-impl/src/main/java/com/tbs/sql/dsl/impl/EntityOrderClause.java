package com.tbs.sql.dsl.impl;

import com.tbs.common.function.SFunction;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.dsl.IOrderClause;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.util.SqlUtils;

import java.util.LinkedList;
import java.util.List;

public class EntityOrderClause<T> implements IOrderClause<T> {
    List<OrderInfo> orderInfos = new LinkedList<>();
    IQueryBuilder<T> queryBuilder;

    public EntityOrderClause(IQueryBuilder<T> queryBuilder) { this.queryBuilder = queryBuilder; }

    @Override
    public <P> IOrderClause<T> orderBy(SFunction<T, P> accessor, OrderEnum order) {
        OrderInfo info = new OrderInfo();
        info.setOrder(order);
        info.setFieldName(SqlUtils.getFieldNameWithMapping(LambdaUtil.getField(accessor)));
        orderInfos.add(info);
        return this;
    }

    @Override public List<OrderInfo> getSqlOrders() { return orderInfos; }
    @Override public IQueryBuilder<T> pour() { return queryBuilder; }
}
