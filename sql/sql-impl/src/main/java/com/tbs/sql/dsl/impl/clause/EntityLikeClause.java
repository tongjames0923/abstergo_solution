package com.tbs.sql.dsl.impl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.ILikeClause;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ContrastEnum;

/**
 * 模糊匹配子句实现类。
 *
 * <p>该类实现了 {@link ILikeClause} 接口，提供全模糊、左模糊、右模糊和NOT LIKE
 * 查询的具体实现。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntityLikeClause<T> implements ILikeClause<T> {

    private final EntityWhereClause<T> parent;

    /**
     * 构造函数。
     *
     * @param parent 父WHERE子句实例
     */
    public EntityLikeClause(EntityWhereClause<T> parent) {
        this.parent = parent;
    }

    @Override
    public <P> IWhereClause<T> like(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.LIKE, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> leftLike(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.LLIKE, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> rightLike(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.RLIKE, value);
        return parent;
    }

    @Override
    public <P> IWhereClause<T> notLike(SFunction<T, P> acc, P value) {
        parent.addCondition(acc, ContrastEnum.NOT_LIKE, value);
        return parent;
    }
}
