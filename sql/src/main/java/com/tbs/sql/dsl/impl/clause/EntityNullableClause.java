package com.tbs.sql.dsl.impl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.INullableClause;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ContrastEnum;

/**
 * 空值判断子句实现类。
 *
 * <p>该类实现了 {@link INullableClause} 接口，提供IS NULL和IS NOT NULL查询的具体实现。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntityNullableClause<T> implements INullableClause<T> {

    private final EntityWhereClause<T> parent;

    /**
     * 构造函数。
     *
     * @param parent 父WHERE子句实例
     */
    public EntityNullableClause(EntityWhereClause<T> parent) {
        this.parent = parent;
    }

    @Override
    public <P> IWhereClause<T> isNull(SFunction<T, P> acc) {
        parent.addCondition(acc, ContrastEnum.IS_NULL, null);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> isNotNull(SFunction<T, P> acc) {
        parent.addCondition(acc, ContrastEnum.IS_NOT_NULL, null);
        return parent;
    }
}
