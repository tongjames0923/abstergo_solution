package com.tbs.sql.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.where.WhereCompose;

/**
 * SQL驱动接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface SqlDriver {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    final class SqlQueryData {
        private SqlBuildContext context;
        private SelectInfoModel selectInfo;
        private List<GroupModel> sqlGroups;
        private LimitModel sqlLimit;
        private List<OrderInfo> sqlOrders;
        private WhereCompose sqlWhereInfo;
    }

    SqlQueryData getSqlQueryData(SqlBuildContext context);
}
