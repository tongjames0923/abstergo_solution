package com.tbs.sql.dsl.impl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.builder.SqlWithParams;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.ISubQueryClause;
import com.tbs.sql.dsl.clause.JoinCondition;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public IWhereClause<T> exists(IQueryBuilder<?> subQuery, JoinCondition<?, ?, ?> joinCondition) {
        return exists(subQuery, new JoinCondition<?, ?, ?>[]{joinCondition});
    }

    @Override
    public IWhereClause<T> exists(IQueryBuilder<?> subQuery, JoinCondition<?, ?, ?>... joinConditions) {
        SqlWithParams sqlWithParams = buildSubQuerySqlWithJoins(subQuery, joinConditions);
        mergeParams(sqlWithParams);
        WhereInfo whereInfo = new WhereInfo();
        whereInfo.setRawSql("EXISTS (" + sqlWithParams.getSql() + ")");
        whereInfo.setContrast(ContrastEnum.EXISTS);
        parent.getCurrentCompose().getChildren().add(
            WhereNode.createInfoNode(ConnectorEnum.AND, whereInfo));
        return parent;
    }

    @Override
    public IWhereClause<T> notExists(IQueryBuilder<?> subQuery, JoinCondition<?, ?, ?> joinCondition) {
        return notExists(subQuery, new JoinCondition<?, ?, ?>[]{joinCondition});
    }

    @Override
    public IWhereClause<T> notExists(IQueryBuilder<?> subQuery, JoinCondition<?, ?, ?>... joinConditions) {
        SqlWithParams sqlWithParams = buildSubQuerySqlWithJoins(subQuery, joinConditions);
        mergeParams(sqlWithParams);
        WhereInfo whereInfo = new WhereInfo();
        whereInfo.setRawSql("NOT EXISTS (" + sqlWithParams.getSql() + ")");
        whereInfo.setContrast(ContrastEnum.NOT_EXISTS);
        parent.getCurrentCompose().getChildren().add(
            WhereNode.createInfoNode(ConnectorEnum.AND, whereInfo));
        return parent;
    }

    /**
     * 构建子查询SQL及其参数。
     *
     * <p>为避免与父查询参数键冲突，子查询参数使用 "sqN_" 前缀（N为子查询索引）。</p>
     * <p>同时为子查询表添加别名，避免字段冲突。</p>
     *
     * @param subQuery 子查询构建器
     * @return SQL语句及其参数的封装对象（参数键已添加前缀，表名已添加别名）
     */
    private SqlWithParams buildSubQuerySql(IQueryBuilder<?> subQuery) {
        SqlWithParams original = subQuery.toSqlWithParams();
        String sql = original.getSql();
        Map<String, Object> params = original.getParams();
        
        // 获取子查询索引，生成唯一前缀
        int sqIndex = parent.nextSubQueryIndex();
        String prefix = "sq" + sqIndex + "_";
        
        // 获取子查询实体类和表名
        Object entity = subQuery.getEntity();
        if (entity != null) {
            Class<?> entityClass = entity.getClass();
            String tableName = SqlUtils.getTableName(entityClass);
            String alias = prefix + tableName;
            
            // 为表名添加别名：FROM tableName -> FROM tableName AS alias
            sql = sql.replaceFirst(
                "FROM\\s+" + tableName + "(\\s|\\b)",
                "FROM " + tableName + " AS " + alias + "$1"
            );
            
            // 为WHERE条件中的字段添加表别名前缀
            // 将WHERE子句中的字段名替换为alias.fieldName
            sql = addAliasToWhereFields(sql, alias);
        }
        
        // 为参数键添加前缀，避免与父查询冲突
        Map<String, Object> prefixedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String newKey = prefix + entry.getKey();
            prefixedParams.put(newKey, entry.getValue());
            // 同时替换SQL中的参数占位符
            sql = sql.replace("#{_prs." + entry.getKey() + "}", "#{_prs." + newKey + "}");
        }
        
        return new SqlWithParams(sql, prefixedParams);
    }

    /**
     * 为WHERE子句中的字段添加表别名前缀。
     *
     * <p>将WHERE子句中的字段名替换为alias.fieldName格式。</p>
     *
     * @param sql 原始SQL
     * @param alias 表别名
     * @return 替换后的SQL
     */
    private String addAliasToWhereFields(String sql, String alias) {
        // 匹配WHERE子句中的字段名（在比较操作符之前）
        // 例如：user_id = #{_prs.param_0} -> sq0_system_user_department_relation.user_id = #{_prs.param_0}
        StringBuilder result = new StringBuilder();
        String[] parts = sql.split("WHERE", 2);
        if (parts.length == 2) {
            result.append(parts[0]);
            result.append("WHERE");
            String whereClause = parts[1];
            // 替换字段名：fieldName -> alias.fieldName
            // 匹配模式：空白或(后跟字段名，然后是比较操作符
            whereClause = whereClause.replaceAll(
                "(?<=\\s|\\()\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b(?=\\s*(?:=|<>|<|>|<=|>=|LIKE|IN|IS|NOT))",
                alias + ".$1"
            );
            result.append(whereClause);
        } else {
            result.append(sql);
        }
        return result.toString();
    }

    /**
     * 构建带关联条件的子查询SQL及其参数。
     *
     * <p>在子查询SQL末尾添加关联条件：sub.field1 = main.field1 AND sub.field2 = main.field2</p>
     *
     * @param subQuery       子查询构建器
     * @param joinConditions 关联条件数组
     * @return SQL语句及其参数的封装对象
     */
    private SqlWithParams buildSubQuerySqlWithJoins(IQueryBuilder<?> subQuery, JoinCondition<?, ?, ?>... joinConditions) {
        // 获取子查询索引，生成唯一前缀
        int sqIndex = parent.nextSubQueryIndex();
        String prefix = "sq" + sqIndex + "_";
        
        // 获取子查询SQL和参数
        SqlWithParams original = subQuery.toSqlWithParams();
        String sql = original.getSql();
        Map<String, Object> params = original.getParams();
        
        // 获取子查询实体类和表名
        Object entity = subQuery.getEntity();
        String subAlias = null;
        if (entity != null) {
            Class<?> entityClass = entity.getClass();
            String tableName = SqlUtils.getTableName(entityClass);
            subAlias = prefix + tableName;
            
            // 为表名添加别名：FROM tableName -> FROM tableName AS alias
            sql = sql.replaceFirst(
                "FROM\\s+" + tableName + "(\\s|\\b)",
                "FROM " + tableName + " AS " + subAlias + "$1"
            );
            
            // 为WHERE条件中的字段添加表别名前缀
            sql = addAliasToWhereFields(sql, subAlias);
        }
        
        // 为参数键添加前缀，避免与父查询冲突
        Map<String, Object> prefixedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String newKey = prefix + entry.getKey();
            prefixedParams.put(newKey, entry.getValue());
            // 同时替换SQL中的参数占位符
            sql = sql.replace("#{_prs." + entry.getKey() + "}", "#{_prs." + newKey + "}");
        }
        
        // 构建关联条件
        if (joinConditions != null && joinConditions.length > 0 && subAlias != null) {
            StringBuilder joinConditionBuilder = new StringBuilder();
            for (int i = 0; i < joinConditions.length; i++) {
                JoinCondition<?, ?, ?> jc = joinConditions[i];
                Field subFieldObj = LambdaUtil.getField(jc.getSubField());
                String subFieldName = SqlUtils.getFieldNameWithMapping(subFieldObj);
                
                Field mainFieldObj = LambdaUtil.getField(jc.getMainField());
                String mainFieldName = SqlUtils.getFieldNameWithMapping(mainFieldObj);
                // 获取主表表名，用于关联条件中明确指定主表字段
                String mainTableName = SqlUtils.getTableName(mainFieldObj.getDeclaringClass());
                
                if (i > 0) {
                    joinConditionBuilder.append(" AND ");
                }
                joinConditionBuilder.append(subAlias).append(".").append(subFieldName)
                    .append(" = ").append(mainTableName).append(".").append(mainFieldName);
            }
            
            String joinCondition = joinConditionBuilder.toString();
            // 将关联条件添加到WHERE子句中
            if (sql.contains("WHERE")) {
                sql = sql.replaceFirst("WHERE", "WHERE " + joinCondition + " AND ");
            } else {
                sql = sql + " WHERE " + joinCondition;
            }
        }
        
        return new SqlWithParams(sql, prefixedParams);
    }

    /**
     * 合并子查询参数到父查询。
     *
     * @param sqlWithParams 子查询的SQL及参数（参数键已添加前缀）
     */
    private void mergeParams(SqlWithParams sqlWithParams) {
        parent.addSubQueryParams(sqlWithParams.getParams());
    }
}
