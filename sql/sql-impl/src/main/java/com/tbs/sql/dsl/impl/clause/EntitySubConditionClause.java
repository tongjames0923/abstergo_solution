package com.tbs.sql.dsl.impl.clause;

import com.tbs.common.function.SConsumer;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.IWhereClause.QueryConnectorEnum;
import com.tbs.sql.dsl.clause.ISubConditionClause;
import com.tbs.sql.dsl.impl.EntityWhereClause;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.model.where.WhereNode;

/**
 * 子条件组合子句实现类。
 *
 * <p>该类实现了 {@link ISubConditionClause} 接口，提供子条件组合的具体实现。</p>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 */
public class EntitySubConditionClause<T> implements ISubConditionClause<T> {

    private final EntityWhereClause<T> parent;

    /**
     * 构造函数。
     *
     * @param parent 父WHERE子句实例
     */
    public EntitySubConditionClause(EntityWhereClause<T> parent) {
        this.parent = parent;
    }

    @Override
    public IWhereClause<T> include(SConsumer<IWhereClause<T>> newCondition, QueryConnectorEnum connectorEnum) {
        EntityWhereClause<T> newConditionProvider = new EntityWhereClause<>(parent.getQueryBuilder());
        newCondition.accept(newConditionProvider);
        ConnectorEnum conn = connectorEnum == QueryConnectorEnum.AND ? ConnectorEnum.AND : ConnectorEnum.OR;
        parent.getCurrentCompose().getChildren().add(new WhereNode<>(conn, newConditionProvider.getCompose()));
        return parent;
    }
}
