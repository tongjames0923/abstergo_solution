package com.tbs.sql.ef.impl;

import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.annotation.SecretField;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.ef.IQueryWhereProvider;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.framework.model.SqlWhereCompose;
import com.tbs.sql.framework.model.SqlWhereInfo;
import com.tbs.sql.framework.model.SqlWhereNode;
import com.tbs.sql.utils.SqlUtils;

import java.lang.reflect.Field;

/**
 * @author tongj
 */
public class EntityQueryWhereProvider<T> implements IQueryWhereProvider<T> {

    private IQueryQueryBuilder<T> queryBuilder;
    SqlWhereCompose currentCompose = new SqlWhereCompose();
    Class<? extends T> targetType;
    QueryConnectorEnum currentConnector = QueryConnectorEnum.AND;

    public EntityQueryWhereProvider(IQueryQueryBuilder<T> queryBuilder) {
        this.queryBuilder = queryBuilder;
        targetType = (Class<? extends T>)queryBuilder.getEntity().getClass();
    }

    @Override
    public IQueryQueryBuilder pour() {
        return queryBuilder;
    }

    @Override
    public IQueryWhereProvider<T> and() {
        currentConnector = QueryConnectorEnum.AND;
        return this;
    }

    @Override
    public IQueryWhereProvider<T> or() {
        currentConnector = QueryConnectorEnum.OR;
        return this;
    }

    @Override
    public IQueryWhereProvider<T> include(SConsumer<IQueryWhereProvider<T>> newCondition,
        QueryConnectorEnum connector) {
        EntityQueryWhereProvider<T> newConditionProvider = new EntityQueryWhereProvider<>(queryBuilder);
        newCondition.accept(newConditionProvider);
        SqlWhereNode<SqlWhereCompose> nextNode = new SqlWhereNode<>(connector, newConditionProvider.getCompose());
        currentCompose.getChildren().add(nextNode);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> isNull(SFunction<T, P> acc) {
        makeInfo(acc, QueryContrastEnum.IS_NULL, null);
        return this;
    }

    private void appendSqlWhereInfo(SqlWhereInfo sqlWhereInfo) {
        SqlWhereNode<SqlWhereInfo> nextNode = new SqlWhereNode<>(currentConnector, sqlWhereInfo);
        getCompose().getChildren().add(nextNode);
    }

    private <P> void initSqlInfo(SFunction<T, P> acc, SqlWhereInfo sqlWhereInfo, P val) {
        Field field = LambdaUtil.getField(acc);
        if (field == null) {
            throw new RuntimeException("无法获取字段数据");
        }
        sqlWhereInfo.setFieldName(SqlUtils.getFiledNameWithMapping(field));
        sqlWhereInfo.setValueType(field.getType());
        sqlWhereInfo.setTableName(SqlUtils.getTableName(targetType));
        sqlWhereInfo.setFieldValue(val);
        if (field.isAnnotationPresent(SecretField.class)) {
            sqlWhereInfo.setUseSafe(true);
        } else {
            sqlWhereInfo.setUseSafe(false);
        }
    }

    @Override
    public <P> IQueryWhereProvider<T> isNotNull(SFunction<T, P> acc) {
        makeInfo(acc, QueryContrastEnum.IS_NOT_NULL, null);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> equal(SFunction<T, P> acc) {
        return equal(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> equal(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.EQUAL, value);
        return this;
    }

    private <P> void makeInfo(SFunction<T, P> acc, QueryContrastEnum contrast, P val) {
        SqlWhereInfo sqlWhereInfo = new SqlWhereInfo();
        initSqlInfo(acc, sqlWhereInfo, val);
        sqlWhereInfo.setContrast(contrast);
        appendSqlWhereInfo(sqlWhereInfo);
    }

    @Override
    public <P> IQueryWhereProvider<T> lessThan(SFunction<T, P> acc) {
        return lessThan(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> lessThan(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.LESS, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> lessThanOrEqual(SFunction<T, P> acc) {
        return lessThanOrEqual(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> lessThanOrEqual(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.LESS_EQUAL, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> greaterThan(SFunction<T, P> acc) {
        return greaterThan(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> greaterThan(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.GREATER, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> greaterThanOrEqual(SFunction<T, P> acc) {
        return greaterThanOrEqual(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> greaterThanOrEqual(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.GREATER_EQUAL, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> notEqual(SFunction<T, P> acc) {
        return notEqual(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> notEqual(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.NOT_EQUAL, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> like(SFunction<T, P> acc) {
        return like(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> like(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.LIKE, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> leftLike(SFunction<T, P> acc) {
        return leftLike(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> leftLike(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.LLIKE, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> rightLike(SFunction<T, P> acc) {
        return rightLike(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> rightLike(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.RLIKE, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> notLike(SFunction<T, P> acc) {
        return notLike(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> notLike(SFunction<T, P> acc, P value) {
        makeInfo(acc, QueryContrastEnum.NOT_LIKE, value);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> in(SFunction<T, P> acc, P values) {
        makeInfo(acc, QueryContrastEnum.IN, values);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> in(SFunction<T, P> acc) {
        return in(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public <P> IQueryWhereProvider<T> notIn(SFunction<T, P> acc, P values) {
        makeInfo(acc, QueryContrastEnum.NOT_IN, values);
        return this;
    }

    @Override
    public <P> IQueryWhereProvider<T> notIn(SFunction<T, P> acc) {

        return notIn(acc, acc.apply(queryBuilder.getEntity()));
    }

    @Override
    public SqlWhereCompose getCompose() {
        return currentCompose;
    }

}
