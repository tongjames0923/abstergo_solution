package com.tbs.sql.builder;

import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.where.WhereCompose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SQL驱动接口，负责从上下文中提取SQL查询数据。
 *
 * <p>该接口定义了SQL驱动的标准方法，从SQL构建上下文中提取SELECT、WHERE、
 * ORDER BY、GROUP BY、LIMIT等查询数据，供后续的SQL生成使用。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从上下文中提取SELECT信息</li>
 *   <li>从上下文中提取WHERE条件</li>
 *   <li>从上下文中提取排序信息</li>
 *   <li>从上下文中提取分组信息</li>
 *   <li>从上下文中提取分页信息</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL构建过程中提取查询数据</li>
 *   <li>为SQL组装器提供数据源</li>
 *   <li>在SQL引擎中驱动SQL生成</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建SQL驱动
 * SqlDriver driver = new EntitySqlDriver();
 *
 * // 从上下文中提取SQL查询数据
 * SqlDriver.SqlQueryData data = driver.getSqlQueryData(context);
 *
 * // 使用提取的数据
 * SelectInfoModel selectInfo = data.getSelectInfo();
 * WhereCompose whereInfo = data.getSqlWhereInfo();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实现类需要确保提取的数据完整且正确</li>
 *   <li>上下文必须有效，否则会抛出异常</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ISqlBuildContext
 * @see AbstractSqlDriver
 * @see EntitySqlDriver
 */
public interface SqlDriver {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    final class SqlQueryData {
        private ISqlBuildContext context;
        private SelectInfoModel selectInfo;
        private List<GroupModel> sqlGroups;
        private LimitModel sqlLimit;
        private List<OrderInfo> sqlOrders;
        private WhereCompose sqlWhereInfo;
    }

    /**
     * 从SQL构建上下文中提取SQL查询数据。
     *
     * @param context SQL构建上下文，包含实体类、参数等信息
     * @return SQL查询数据，包含SELECT、WHERE、ORDER BY、GROUP BY、LIMIT等信息
     */
    SqlQueryData getSqlQueryData(ISqlBuildContext context);
}
