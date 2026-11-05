package com.tbs.sql.dsl.impl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.IComparisonClause;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ContrastEnum;

/**
 * 比较条件子句实现类。
 *
 * <p>该类实现了 {@link IComparisonClause} 接口，提供等值、不等于、大小比较等
 * 二元比较运算的具体实现。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntityComparisonClause<T> implements IComparisonClause<T> {

    private final EntityWhereClause<T> parent;

    /**
     * 构造函数。
     *
     * @param parent 父WHERE子句实例
     */
    public EntityComparisonClause(EntityWhereClause<T> parent) {
        this.parent = parent;
    }

    @Override
    public <P> IWhereClause<T> equal(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.EQUAL, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> notEqual(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.NOT_EQUAL, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> lessThan(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.LESS, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.LESS_EQUAL, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> greaterThan(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.GREATER, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.GREATER_EQUAL, value);
        return parent;
    }
}
