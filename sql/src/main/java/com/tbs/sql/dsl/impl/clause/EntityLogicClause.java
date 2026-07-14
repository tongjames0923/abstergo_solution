package com.tbs.sql.dsl.impl.clause;

import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.clause.ILogicClause;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ConnectorEnum;

/**
 * 逻辑连接子句实现类。
 *
 * <p>该类实现了 {@link ILogicClause} 接口，提供AND和OR逻辑连接符的具体实现。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntityLogicClause<T> implements ILogicClause<T> {

    private final EntityWhereClause<T> parent;

    /**
     * 构造函数。
     *
     * @param parent 父WHERE子句实例
     */
    public EntityLogicClause(EntityWhereClause<T> parent) {
        this.parent = parent;
    }

    @Override
    public IWhereClause<T> and() {
        parent.setCurrentConnector(ConnectorEnum.AND);
        return parent;
    }

    @Override
    public IWhereClause<T> or() {
        parent.setCurrentConnector(ConnectorEnum.OR);
        return parent;
    }
}
