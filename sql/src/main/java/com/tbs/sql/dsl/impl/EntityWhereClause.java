package com.tbs.sql.dsl.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.annotation.mapping.SecretField;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;

import java.lang.reflect.Field;

public class EntityWhereClause<T> implements IWhereClause<T> {
    private IQueryBuilder<T> queryBuilder;
    WhereCompose currentCompose = new WhereCompose();
    Class<? extends T> targetType;
    ConnectorEnum currentConnector = ConnectorEnum.AND;

    public EntityWhereClause(IQueryBuilder<T> queryBuilder) {
        this.queryBuilder = queryBuilder;
        targetType = (Class<? extends T>) queryBuilder.getEntity().getClass();
    }

    @Override public IQueryBuilder<T> pour() { return queryBuilder; }
    @Override public IWhereClause<T> and() { currentConnector = ConnectorEnum.AND; return this; }
    @Override public IWhereClause<T> or() { currentConnector = ConnectorEnum.OR; return this; }

    @Override
    public IWhereClause<T> include(SConsumer<IWhereClause<T>> newCondition, QueryConnectorEnum connector) {
        EntityWhereClause<T> newConditionProvider = new EntityWhereClause<>(queryBuilder);
        newCondition.accept(newConditionProvider);
        ConnectorEnum conn = connector == QueryConnectorEnum.AND ? ConnectorEnum.AND : ConnectorEnum.OR;
        currentCompose.getChildren().add(new WhereNode<>(conn, newConditionProvider.getCompose()));
        return this;
    }

    private void appendWhereInfo(WhereInfo whereInfo) {
        currentCompose.getChildren().add(new WhereNode<>(currentConnector, whereInfo));
    }

    private <P> void initSqlInfo(SFunction<T, P> acc, WhereInfo whereInfo, P val) {
        Field field = LambdaUtil.getField(acc);
        AssertUtil.isTrue(field == null).message("无法获取字段数据").errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR).throwIt();
        whereInfo.setFieldName(SqlUtils.getFieldNameWithMapping(field));
        whereInfo.setValueType(field.getType());
        whereInfo.setTableName(SqlUtils.getTableName(targetType));
        whereInfo.setFieldValue(val);
        whereInfo.setUseSafe(field.isAnnotationPresent(SecretField.class));
    }

    private <P> void makeInfo(SFunction<T, P> acc, ContrastEnum contrast, P val) {
        WhereInfo whereInfo = new WhereInfo();
        initSqlInfo(acc, whereInfo, val);
        whereInfo.setContrast(contrast);
        appendWhereInfo(whereInfo);
    }

    @Override public <P> IWhereClause<T> isNull(SFunction<T, P> acc) { makeInfo(acc, ContrastEnum.IS_NULL, null); return this; }
    @Override public <P> IWhereClause<T> isNotNull(SFunction<T, P> acc) { makeInfo(acc, ContrastEnum.IS_NOT_NULL, null); return this; }
    @Override public <P> IWhereClause<T> equal(SFunction<T, P> acc) { return equal(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> equal(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.EQUAL, value); return this; }
    @Override public <P> IWhereClause<T> lessThan(SFunction<T, P> acc) { return lessThan(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> lessThan(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.LESS, value); return this; }
    @Override public <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc) { return lessThanOrEqual(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.LESS_EQUAL, value); return this; }
    @Override public <P> IWhereClause<T> greaterThan(SFunction<T, P> acc) { return greaterThan(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> greaterThan(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.GREATER, value); return this; }
    @Override public <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc) { return greaterThanOrEqual(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.GREATER_EQUAL, value); return this; }
    @Override public <P> IWhereClause<T> notEqual(SFunction<T, P> acc) { return notEqual(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> notEqual(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.NOT_EQUAL, value); return this; }
    @Override public <P> IWhereClause<T> like(SFunction<T, P> acc) { return like(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> like(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.LIKE, value); return this; }
    @Override public <P> IWhereClause<T> leftLike(SFunction<T, P> acc) { return leftLike(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> leftLike(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.LLIKE, value); return this; }
    @Override public <P> IWhereClause<T> rightLike(SFunction<T, P> acc) { return rightLike(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> rightLike(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.RLIKE, value); return this; }
    @Override public <P> IWhereClause<T> notLike(SFunction<T, P> acc) { return notLike(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> notLike(SFunction<T, P> acc, P value) { makeInfo(acc, ContrastEnum.NOT_LIKE, value); return this; }
    @Override public <P> IWhereClause<T> in(SFunction<T, P> acc, P values) { makeInfo(acc, ContrastEnum.IN, values); return this; }
    @Override public <P> IWhereClause<T> in(SFunction<T, P> acc) { return in(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public <P> IWhereClause<T> notIn(SFunction<T, P> acc, P values) { makeInfo(acc, ContrastEnum.NOT_IN, values); return this; }
    @Override public <P> IWhereClause<T> notIn(SFunction<T, P> acc) { return notIn(acc, acc.apply(queryBuilder.getEntity())); }
    @Override public WhereCompose getCompose() { return currentCompose; }
}
