package com.tbs.sql.dsl.impl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.builder.SqlWithParams;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.ISubQueryClause;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;

import java.lang.reflect.Field;

/**
 * 子查询条件子句实现类。
 *
 * <p>该类实现了 {@link ISubQueryClause} 接口，提供跨实体的子查询条件的具体实现。
 * 支持EXISTS、NOT EXISTS、IN子查询、NOT IN子查询等跨实体查询条件。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntitySubQueryClause<T> implements ISubQueryClause<T> {

    private final EntityWhereClause<T> parent;

    /**
     * 构造函数。
     *
     * @param parent 父WHERE子句实例
     */
    public EntitySubQueryClause(EntityWhereClause<T> parent) {
        this.parent = parent;
    }

    @Override
    public IWhereClause<T> exists(IQueryBuilder<?> subQuery) {
        SqlWithParams sqlWithParams = buildSubQuerySql(subQuery);
        mergeParams(sqlWithParams);
        WhereInfo whereInfo = new WhereInfo();
        whereInfo.setRawSql("EXISTS (" + sqlWithParams.getSql() + ")");
        whereInfo.setContrast(ContrastEnum.EXISTS);
        parent.getCurrentCompose().getChildren().add(
            WhereNode.createInfoNode(ConnectorEnum.AND, whereInfo));
        return parent;
    }

    @Override
    public IWhereClause<T> notExists(IQueryBuilder<?> subQuery) {
        SqlWithParams sqlWithParams = buildSubQuerySql(subQuery);
        mergeParams(sqlWithParams);
        WhereInfo whereInfo = new WhereInfo();
        whereInfo.setRawSql("NOT EXISTS (" + sqlWithParams.getSql() + ")");
        whereInfo.setContrast(ContrastEnum.NOT_EXISTS);
        parent.getCurrentCompose().getChildren().add(
            WhereNode.createInfoNode(ConnectorEnum.AND, whereInfo));
        return parent;
    }

    @Override
    public <P> IWhereClause<T> in(SFunction<T, P> acc, IQueryBuilder<?> subQuery) {
        SqlWithParams sqlWithParams = buildSubQuerySql(subQuery);
        mergeParams(sqlWithParams);
        Field field = LambdaUtil.getField(acc);
        String fieldName = SqlUtils.getFieldNameWithMapping(field);
        String tableName = SqlUtils.getTableName(field.getDeclaringClass());

        WhereInfo whereInfo = new WhereInfo();
        whereInfo.setFieldName(tableName + "." + fieldName);
        whereInfo.setRawSql("(" + sqlWithParams.getSql() + ")");
        whereInfo.setContrast(ContrastEnum.IN_SUBQUERY);
        whereInfo.setValueType(field.getType());
        parent.getCurrentCompose().getChildren().add(
            WhereNode.createInfoNode(ConnectorEnum.AND, whereInfo));
        return parent;
    }

    @Override
    public <P> IWhereClause<T> notIn(SFunction<T, P> acc, IQueryBuilder<?> subQuery) {
        SqlWithParams sqlWithParams = buildSubQuerySql(subQuery);
        mergeParams(sqlWithParams);
        Field field = LambdaUtil.getField(acc);
        String fieldName = SqlUtils.getFieldNameWithMapping(field);
        String tableName = SqlUtils.getTableName(field.getDeclaringClass());

        WhereInfo whereInfo = new WhereInfo();
        whereInfo.setFieldName(tableName + "." + fieldName);
        whereInfo.setRawSql("(" + sqlWithParams.getSql() + ")");
        whereInfo.setContrast(ContrastEnum.NOT_IN_SUBQUERY);
        whereInfo.setValueType(field.getType());
        parent.getCurrentCompose().getChildren().add(
            WhereNode.createInfoNode(ConnectorEnum.AND, whereInfo));
        return parent;
    }

    /**
     * 构建子查询SQL及其参数。
     *
     * @param subQuery 子查询构建器
     * @return SQL语句及其参数的封装对象
     */
    private SqlWithParams buildSubQuerySql(IQueryBuilder<?> subQuery) {
        return subQuery.toSqlWithParams();
    }

    /**
     * 合并子查询参数到父查询。
     *
     * @param sqlWithParams 子查询的SQL及参数
     */
    private void mergeParams(SqlWithParams sqlWithParams) {
        parent.addSubQueryParams(sqlWithParams.getParams());
    }
}
