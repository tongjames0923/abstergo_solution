package com.tbs.sql.dsl.impl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.IInClause;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ContrastEnum;

/**
 * 集合条件子句实现类。
 *
 * <p>该类实现了 {@link IInClause} 接口，提供IN和NOT IN查询的具体实现。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntityInClause<T> implements IInClause<T> {

    private final EntityWhereClause<T> parent;

    /**
     * 构造函数。
     *
     * @param parent 父WHERE子句实例
     */
    public EntityInClause(EntityWhereClause<T> parent) {
        this.parent = parent;
    }

    @Override
    public <P> IWhereClause<T> in(SFunction<T, P> acc, P values) {
        parent.addCondition(acc, ContrastEnum.IN, values);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> notIn(SFunction<T, P> acc, P values) {
        parent.addCondition(acc, ContrastEnum.NOT_IN, values);
        return parent;
    }
}
