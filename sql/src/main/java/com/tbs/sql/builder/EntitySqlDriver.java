package com.tbs.sql.builder;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.where.WhereCompose;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体查询SQL驱动
 *
 * @author abstergo
 * @since 2.0.0
 */
public class EntitySqlDriver extends AbstractSqlDriver {
    @Setter List<GroupModel> sqlGroups;
    private WhereCompose compose = new WhereCompose();
    private LimitModel limit = new LimitModel();
    private List<OrderInfo> orders = new ArrayList<>();

    public EntitySqlDriver setWhereInfo(WhereCompose compose) { this.compose = compose; return this; }
    public EntitySqlDriver setLimit(LimitModel limit) { this.limit = limit; return this; }
    public EntitySqlDriver setOrders(List<OrderInfo> orders) { this.orders = orders; return this; }

    @Override
    protected SelectInfoModel getSelectInfo(SqlBuildContext context) {
        SelectInfoModel selectInfoModel;
        if (BeanUtil.isBaseFrom(context.getEntityClass(), IQueryCriteria.class)) {
            selectInfoModel = ((IQueryCriteria) context.getSqlEntity()).getSelectSQLInfo();
        } else {
            selectInfoModel = EntityMetadataExtractor.pullFromClass(context.getEntityClass());
        }
        selectInfoModel.setEntityClass(context.getEntityClass());
        return selectInfoModel;
    }

    @Override protected WhereCompose getWhereInfo(SqlBuildContext context) { return compose; }
    @Override protected List<GroupModel> getGroups(SqlBuildContext context) { return this.sqlGroups; }
    @Override protected LimitModel getLimit(SqlBuildContext context) { return limit; }
    @Override protected List<OrderInfo> getOrders(SqlBuildContext context) { return orders; }
}
