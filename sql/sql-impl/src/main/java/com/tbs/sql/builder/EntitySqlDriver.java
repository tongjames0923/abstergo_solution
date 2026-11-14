package com.tbs.sql.builder;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;
import com.tbs.sql.model.where.WhereCompose;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    /** 投影：仅返回的列（物理列名），为空表示不限制 */
    private Set<String> includeColumns;
    /** 投影：需排除的列（物理列名），为空表示不排除 */
    private Set<String> excludeColumns;

    public EntitySqlDriver setWhereInfo(WhereCompose compose) { this.compose = compose; return this; }
    public EntitySqlDriver setLimit(LimitModel limit) { this.limit = limit; return this; }
    public EntitySqlDriver setOrders(List<OrderInfo> orders) { this.orders = orders; return this; }
    public EntitySqlDriver setMainQuery(boolean mainQuery) { this.mainQuery = mainQuery; return this; }

    /**
     * 设置列投影。
     *
     * @param includeColumns 仅返回的列（物理列名），null 或空表示不限制
     * @param excludeColumns 需排除的列（物理列名），null 或空表示不排除
     * @return 当前驱动实例
     */
    public EntitySqlDriver setProjection(Set<String> includeColumns, Set<String> excludeColumns) {
        this.includeColumns = includeColumns;
        this.excludeColumns = excludeColumns;
        return this;
    }

    @Override
    protected SelectInfoModel getSelectInfo(ISqlBuildContext context) {
        if (BeanUtil.isBaseFrom(context.getEntityClass(), IQueryCriteria.class)) {
            // 条件对象可能按状态动态生成 SELECT 信息，不参与片段缓存
            SelectInfoModel dynamic = ((IQueryCriteria) context.getSqlEntity()).getSelectSQLInfo();
            dynamic.setEntityClass(context.getEntityClass());
            return dynamic;
        }

        SelectInfoModel cached = EntityMetadataExtractor.pullFromClass(context.getEntityClass());
        List<SelectItemModel> projected = applyProjection(cached.getFieldList());
        if (projected != cached.getFieldList()) {
            // 有列投影：副本内容随投影变化，不可复用片段缓存
            SelectInfoModel copy = new SelectInfoModel(cached.getRawSql(), cached.getTableName(),
                cached.getTableAlias(), projected, cached.getEntityClass());
            copy.setEntityClass(context.getEntityClass());
            return copy;
        }

        // 无投影：直接复用元数据缓存对象（pullFromClass 已将其标记为可缓存），
        // 既省去每次查询的副本分配，也让 SELECT 片段缓存能跨查询命中。
        // 本路径对该对象只读，缓存写入的 transient 字段不改变业务语义。
        return cached;
    }

    /**
     * 根据 include/exclude 投影集合过滤字段列表。
     * <p>
     * 无任何投影时直接返回原列表（仅读使用）；有投影时生成新列表，绝不修改传入的缓存列表。
     * 规则：先以 include 为基集（include 为空则基集为全部），再剔除 exclude 命中的列。
     * </p>
     *
     * @param source 原始字段列表（可能为共享缓存）
     * @return 过滤后的字段列表
     */
    private List<SelectItemModel> applyProjection(List<SelectItemModel> source) {
        if (source == null) {
            return null;
        }
        boolean hasInclude = includeColumns != null && !includeColumns.isEmpty();
        boolean hasExclude = excludeColumns != null && !excludeColumns.isEmpty();
        if (!hasInclude && !hasExclude) {
            return source;
        }
        List<SelectItemModel> result = new ArrayList<>(source.size());
        for (SelectItemModel item : source) {
            String column = item.getFieldName();
            if (hasInclude && !includeColumns.contains(column)) {
                continue;
            }
            if (hasExclude && excludeColumns.contains(column)) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    @Override protected WhereCompose getWhereInfo(ISqlBuildContext context) { return compose; }
    @Override protected List<GroupModel> getGroups(ISqlBuildContext context) { return this.sqlGroups; }
    @Override protected LimitModel getLimit(ISqlBuildContext context) { return limit; }
    @Override protected List<OrderInfo> getOrders(ISqlBuildContext context) { return orders; }
}
