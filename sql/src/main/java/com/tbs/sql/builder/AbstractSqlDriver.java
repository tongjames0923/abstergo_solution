package com.tbs.sql.builder;

import java.util.List;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.where.WhereCompose;
import org.jetbrains.annotations.NotNull;

/**
 * SQL驱动抽象类
 *
 * @author abstergo
 * @since 2.0.0
 */
public abstract class AbstractSqlDriver implements SqlDriver {
    protected abstract @NotNull SelectInfoModel getSelectInfo(SqlBuildContext context);
    protected abstract @NotNull WhereCompose getWhereInfo(SqlBuildContext context);
    protected abstract @NotNull List<GroupModel> getGroups(SqlBuildContext context);
    protected abstract @NotNull LimitModel getLimit(SqlBuildContext context);
    protected abstract @NotNull List<OrderInfo> getOrders(SqlBuildContext context);

    protected boolean supportContext(SqlBuildContext context) { return true; }

    @Override
    public SqlQueryData getSqlQueryData(SqlBuildContext context) {
        AssertUtil.notNull(context, "环境上下文不能为空");
        AssertUtil.isTrue(!supportContext(context), "不支持该环境上下文{0}", context);
        AssertUtil.isTrue(!context.isValid(), "环境上下文{0}已失效", context);
        return new SqlQueryData(context, getSelectInfo(context), getGroups(context), getLimit(context), getOrders(context), getWhereInfo(context));
    }
}
