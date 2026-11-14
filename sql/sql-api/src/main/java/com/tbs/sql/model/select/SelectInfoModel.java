package com.tbs.sql.model.select;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * SQL查询信息模型类，封装SQL查询的各种信息。
 *
 * <p>该类封装了SQL查询所需的所有信息，包括原始SQL语句、表名、字段列表和实体类。
 * 用于在SQL构建过程中传递和存储查询信息。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>存储原始SQL语句 {@link #getRawSql()}</li>
 *   <li>存储表名 {@link #getTableName()}</li>
 *   <li>存储字段列表 {@link #getFieldList()}</li>
 *   <li>存储实体类 {@link #getEntityClass()}</li>
 *   <li>提供静态工厂方法创建实例</li>
 *   <li>可选的 SELECT 片段缓存，见 {@link #markSelectCacheable()}</li>
 * </ul>
 *
 * <h3>SELECT 片段缓存</h3>
 * <p>只有被 {@code EntityMetadataExtractor} 长期持有、且结构不再变化的实例才能调用
 * {@link #markSelectCacheable()} 开启缓存。方言提取器以自身实例为 owner 写入不可变快照，
 * 命中时直接返回已渲染的 SELECT 片段，不再遍历字段列表。</p>
 * <p>快照同时记录渲染时的字段引用，任一字段被 setter 替换即自动失效；
 * 但直接修改 {@code fieldList} 集合内容不会被检测，因此被标记的实例应按不可变对象使用。
 * 缓存字段均为 transient，不参与序列化、equals/hashCode/toString。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建原始SQL查询信息
 * SelectInfoModel selectInfo = SelectInfoModel.rawSql("SELECT * FROM users");
 *
 * // 创建表名查询信息
 * SelectInfoModel selectInfo = SelectInfoModel.tableName("users");
 *
 * // 创建自定义查询信息
 * SelectInfoModel selectInfo = SelectInfoModel.custom("users", fieldList);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>原始SQL和表名二选一使用</li>
 *   <li>字段列表可以为空</li>
 *   <li>支持序列化</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SelectItemModel
 * @see com.tbs.sql.metadata.EntityMetadataExtractor
 */
@Data
@NoArgsConstructor
public class SelectInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 原始SQL语句 */
    private String rawSql;
    /** 表名 */
    private String tableName;
    /** 表别名 */
    private String tableAlias;
    /** 字段列表 */
    List<SelectItemModel> fieldList;
    /** 实体类 */
    private Class<?> entityClass;

    /** 是否允许缓存 SELECT 片段：仅元数据缓存对象会被标记 */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private transient boolean selectCacheable;

    /** 缓存快照：volatile + 不可变，并发写入是幂等的，最多重复一次渲染 */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private transient volatile SelectFragment selectFragment;

    public SelectInfoModel(String rawSql, String tableName, String tableAlias, List<SelectItemModel> fieldList, Class<?> entityClass) {
        this.rawSql = rawSql;
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.fieldList = fieldList;
        this.entityClass = entityClass;
    }

    public static SelectInfoModel rawSql(String sql) {
        return new SelectInfoModel(sql, null, null, null, null);
    }

    public static SelectInfoModel tableName(String tableName) {
        return new SelectInfoModel(null, tableName, null, null, null);
    }

    public static SelectInfoModel tableNameWithAlias(String tableName, String tableAlias) {
        return new SelectInfoModel(null, tableName, tableAlias, null, null);
    }

    public static SelectInfoModel custom(String tableName, List<SelectItemModel> fieldList) {
        return new SelectInfoModel(null, tableName, null, fieldList, null);
    }

    /**
     * 标记本实例结构稳定、可缓存 SELECT 片段。
     *
     * <p>仅应由长期持有且不再修改的模型调用（如实体类元数据缓存）；
     * 动态构造、带列投影、以及 {@code IQueryCriteria#getSelectSQLInfo()} 返回的模型不要标记。</p>
     */
    public void markSelectCacheable() {
        selectCacheable = true;
    }

    /**
     * 读取指定方言提取器写入的 SELECT 片段。
     *
     * @param owner 方言提取器实例，防止不同方言（如 PostgreSQL 别名加引号）互相复用片段
     * @return 命中时返回缓存片段，否则返回 null
     */
    public String cachedSelectSql(Object owner) {
        SelectFragment fragment = selectFragment;
        if (fragment == null || fragment.owner != owner) {
            return null;
        }
        // 引用级失效校验：setter 替换了任一结构字段就重新渲染
        if (fragment.rawSql != rawSql || fragment.tableName != tableName
                || fragment.tableAlias != tableAlias || fragment.fieldList != fieldList) {
            return null;
        }
        return fragment.sql;
    }

    /**
     * 写入 SELECT 片段缓存快照；未标记或入参为空时静默跳过。
     *
     * @param owner 方言提取器实例
     * @param sql   已渲染的 SELECT 片段
     */
    public void cacheSelectSql(Object owner, String sql) {
        if (!selectCacheable || owner == null || sql == null) {
            return;
        }
        selectFragment = new SelectFragment(owner, sql, rawSql, tableName, tableAlias, fieldList);
    }

    /** 不可变缓存快照：owner 与渲染时的结构字段引用一并记录 */
    private static final class SelectFragment {
        private final Object owner;
        private final String sql;
        private final String rawSql;
        private final String tableName;
        private final String tableAlias;
        private final List<SelectItemModel> fieldList;

        SelectFragment(Object owner, String sql, String rawSql, String tableName,
                       String tableAlias, List<SelectItemModel> fieldList) {
            this.owner = owner;
            this.sql = sql;
            this.rawSql = rawSql;
            this.tableName = tableName;
            this.tableAlias = tableAlias;
            this.fieldList = fieldList;
        }
    }
}
