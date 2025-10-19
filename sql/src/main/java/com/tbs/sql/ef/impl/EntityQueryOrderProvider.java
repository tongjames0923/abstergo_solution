package com.tbs.sql.ef.impl;

import com.tbs.common.function.SFunction;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.ef.IQueryOrderProvider;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.framework.model.SqlOrderInfo;
import com.tbs.sql.utils.SqlUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 排序功能提供器
 *
 * @author abstergo
 */
public class EntityQueryOrderProvider<T> implements IQueryOrderProvider<T> {

    List<SqlOrderInfo> orderInfos = new LinkedList<>();

    IQueryQueryBuilder<T> queryBuilder;

    public EntityQueryOrderProvider(IQueryQueryBuilder<T> queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    public <P> IQueryOrderProvider<T> orderBy(SFunction<T, P> accessor, QueryOrderEnum order) {
        SqlOrderInfo info = new SqlOrderInfo();
        info.setOrder(order);
        info.setFiledName(SqlUtils.getFiledNameWithMapping(LambdaUtil.getField(accessor)));
        orderInfos.add(info);
        return this;
    }

    @Override
    public List<SqlOrderInfo> getSqlOrders() {
        return orderInfos;
    }

    @Override
    public IQueryQueryBuilder<T> pour() {
        return queryBuilder;
    }
}
