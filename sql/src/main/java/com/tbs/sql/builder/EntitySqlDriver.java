package com.tbs.sql.builder;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.util.SqlUtils;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体查询SQL驱动类，负责从实体类中提取SQL查询数据。
 *
 * <p>该类继承自 {@link AbstractSqlDriver}，专门用于处理实体类查询。它从实体类中
 * 提取SELECT信息、WHERE条件、分组、排序和分页信息，支持基于注解的自动提取。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从实体类元数据中提取SELECT信息</li>
 *   <li>从查询条件中提取WHERE子句</li>
 *   <li>提取分组、排序和分页信息</li>
 *   <li>支持 {@link IQueryCriteria} 接口的查询类</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>处理基于实体类的查询</li>
 *   <li>处理基于查询条件的查询</li>
 *   <li>在SQL引擎中驱动实体查询</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * EntitySqlDriver driver = new EntitySqlDriver();
 * driver.setWhereInfo(whereCompose);
 * driver.setOrders(orderInfos);
 * driver.setLimit(limitModel);
 * 
 * // 从上下文中提取SQL查询数据
 * SqlDriver.SqlQueryData data = driver.getSqlQueryData(context);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要确保实体类正确配置了表名和字段映射</li>
 *   <li>查询条件需要正确设置，否则可能生成错误的SQL</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see AbstractSqlDriver
 * @see SqlDriver
 * @see IQueryCriteria
 */
public class EntitySqlDriver extends AbstractSqlDriver {
    @Setter List<GroupModel> sqlGroups;
    private WhereCompose compose = new WhereCompose();
    private LimitModel limit = new LimitModel();
    private List<OrderInfo> orders = new ArrayList<>();
    /** 是否为主查询（主查询使用main_别名，子查询不使用） */
    private boolean mainQuery = true;

    public EntitySqlDriver setWhereInfo(WhereCompose compose) { this.compose = compose; return this; }
    public EntitySqlDriver setLimit(LimitModel limit) { this.limit = limit; return this; }
    public EntitySqlDriver setOrders(List<OrderInfo> orders) { this.orders = orders; return this; }
    public EntitySqlDriver setMainQuery(boolean mainQuery) { this.mainQuery = mainQuery; return this; }

    @Override
    protected SelectInfoModel getSelectInfo(SqlBuildContext context) {
        SelectInfoModel selectInfoModel;
        if (BeanUtil.isBaseFrom(context.getEntityClass(), IQueryCriteria.class)) {
            selectInfoModel = ((IQueryCriteria) context.getSqlEntity()).getSelectSQLInfo();
        } else {
            // 创建副本，避免修改缓存中的对象
            SelectInfoModel cached = EntityMetadataExtractor.pullFromClass(context.getEntityClass());
            selectInfoModel = new SelectInfoModel(
                cached.getRawSql(),
                cached.getTableName(),
                cached.getTableAlias(),
                cached.getFieldList(),
                cached.getEntityClass()
            );
        }
        selectInfoModel.setEntityClass(context.getEntityClass());
        return selectInfoModel;
    }

    @Override protected WhereCompose getWhereInfo(SqlBuildContext context) { return compose; }
    @Override protected List<GroupModel> getGroups(SqlBuildContext context) { return this.sqlGroups; }
    @Override protected LimitModel getLimit(SqlBuildContext context) { return limit; }
    @Override protected List<OrderInfo> getOrders(SqlBuildContext context) { return orders; }
}
