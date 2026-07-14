package com.tbs.sql.dsl.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.function.SFunction;
import com.tbs.common.util.function.LambdaUtil;
import com.tbs.sql.annotation.mapping.SecretField;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.*;
import com.tbs.sql.dsl.impl.clause.*;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;

import java.lang.reflect.Field;

/**
 * WHERE子句实现类，采用组合模式实现。
 *
 * <p>该类实现了 {@link IWhereClause} 接口，通过聚合多个子句实例提供查询条件构建功能。
 * 使用懒加载模式创建子句实例，避免不必要的对象创建。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntityWhereClause<T> implements IWhereClause<T> {

    private final IQueryBuilder<T> queryBuilder;
    private final WhereCompose currentCompose = new WhereCompose();
    private final Class<? extends T> targetType;
    private ConnectorEnum currentConnector = ConnectorEnum.AND;

    // 子句实例（懒加载）
    private IComparisonClause<T> comparisonClause;
    private ILikeClause<T> likeClause;
    private IInClause<T> inClause;
    private INullableClause<T> nullableClause;
    private ISubConditionClause<T> subConditionClause;
    private ILogicClause<T> logicClause;

    /**
     * 构造函数。
     *
     * @param queryBuilder 查询构建器实例
     */
    public EntityWhereClause(IQueryBuilder<T> queryBuilder) {
        this.queryBuilder = queryBuilder;
        this.targetType = (Class<? extends T>)queryBuilder.getEntity().getClass();
    }

    // ============ 子句访问方法（懒加载）============

    @Override
    public IComparisonClause<T> comparison() {
        if (comparisonClause == null) {
            comparisonClause = new EntityComparisonClause<>(this);
        }
        return comparisonClause;
    }

    @Override
    public ILikeClause<T> like() {
        if (likeClause == null) {
            likeClause = new EntityLikeClause<>(this);
        }
        return likeClause;
    }

    @Override
    public IInClause<T> in() {
        if (inClause == null) {
            inClause = new EntityInClause<>(this);
        }
        return inClause;
    }

    @Override
    public INullableClause<T> nullable() {
        if (nullableClause == null) {
            nullableClause = new EntityNullableClause<>(this);
        }
        return nullableClause;
    }

    @Override
    public ISubConditionClause<T> sub() {
        if (subConditionClause == null) {
            subConditionClause = new EntitySubConditionClause<>(this);
        }
        return subConditionClause;
    }

    @Override
    public ILogicClause<T> logic() {
        if (logicClause == null) {
            logicClause = new EntityLogicClause<>(this);
        }
        return logicClause;
    }

    // ============ 核心方法 ============

    @Override
    public IQueryBuilder<T> pour() {
        return queryBuilder;
    }

    @Override
    public WhereCompose getCompose() {
        return currentCompose;
    }

    // ============ 内部方法（供子句调用）============

    /**
     * 获取查询构建器。
     *
     * @return 查询构建器实例
     */
    public IQueryBuilder<T> getQueryBuilder() {
        return queryBuilder;
    }

    /**
     * 获取当前WHERE条件组合。
     *
     * @return WHERE条件组合实例
     */
    public WhereCompose getCurrentCompose() {
        return currentCompose;
    }

    /**
     * 设置当前连接器类型。
     *
     * @param connector 连接器类型
     */
    public void setCurrentConnector(ConnectorEnum connector) {
        this.currentConnector = connector;
    }

    /**
     * 添加查询条件。
     *
     * @param acc      字段引用函数
     * @param contrast 比较类型
     * @param value    比较值
     * @param <P>      字段类型
     */
    public <P> void addCondition(SFunction<T, P> acc, ContrastEnum contrast, P value) {
        WhereInfo whereInfo = new WhereInfo();
        initSqlInfo(acc, whereInfo, value);
        whereInfo.setContrast(contrast);
        appendWhereInfo(whereInfo);
    }

    // ============ 私有方法 ============

    private void appendWhereInfo(WhereInfo whereInfo) {
        currentCompose.getChildren().add(new WhereNode<>(currentConnector, whereInfo));
    }

    private <P> void initSqlInfo(SFunction<T, P> acc, WhereInfo whereInfo, P val) {
        Field field = LambdaUtil.getField(acc);
        AssertUtil.isTrue(field == null).message("无法获取字段数据").errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR)
            .throwIt();
        whereInfo.setFieldName(SqlUtils.getFieldNameWithMapping(field));
        whereInfo.setValueType(field.getType());
        whereInfo.setTableName(SqlUtils.getTableName(targetType));
        whereInfo.setFieldValue(val);
        whereInfo.setUseSafe(field.isAnnotationPresent(SecretField.class));
    }
}
