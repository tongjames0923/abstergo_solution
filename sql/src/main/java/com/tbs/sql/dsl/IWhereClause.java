package com.tbs.sql.dsl;

import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.model.where.WhereCompose;

/**
 * WHERE子句接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface IWhereClause<T> extends IFeatureProviderPour<T> {
    IWhereClause<T> and();
    IWhereClause<T> or();
    IWhereClause<T> include(SConsumer<IWhereClause<T>> newCondition, QueryConnectorEnum connectorEnum);
    default IWhereClause<T> andInclude(SConsumer<IWhereClause<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.AND);
    }
    default IWhereClause<T> orInclude(SConsumer<IWhereClause<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.OR);
    }
    <P> IWhereClause<T> isNull(SFunction<T, P> acc);
    <P> IWhereClause<T> isNotNull(SFunction<T, P> acc);
    <P> IWhereClause<T> equal(SFunction<T, P> acc);
    <P> IWhereClause<T> equal(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> lessThan(SFunction<T, P> acc);
    <P> IWhereClause<T> lessThan(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc);
    <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> greaterThan(SFunction<T, P> acc);
    <P> IWhereClause<T> greaterThan(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc);
    <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> notEqual(SFunction<T, P> acc);
    <P> IWhereClause<T> notEqual(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> like(SFunction<T, P> acc);
    <P> IWhereClause<T> like(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> leftLike(SFunction<T, P> acc);
    <P> IWhereClause<T> leftLike(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> rightLike(SFunction<T, P> acc);
    <P> IWhereClause<T> rightLike(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> notLike(SFunction<T, P> acc);
    <P> IWhereClause<T> notLike(SFunction<T, P> acc, P value);
    <P> IWhereClause<T> in(SFunction<T, P> acc, P values);
    <P> IWhereClause<T> in(SFunction<T, P> acc);
    <P> IWhereClause<T> notIn(SFunction<T, P> acc, P values);
    <P> IWhereClause<T> notIn(SFunction<T, P> acc);
    WhereCompose getCompose();

    /** 兼容旧枚举名的连接器枚举 */
    enum QueryConnectorEnum { AND, OR }
}
