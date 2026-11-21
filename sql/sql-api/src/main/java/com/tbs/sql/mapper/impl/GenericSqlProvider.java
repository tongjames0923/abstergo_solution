package com.tbs.sql.mapper.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.dialect.IInsertExtractor;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.mapper.GenericMapper;
import com.tbs.sql.mapper.MapperEntityTypeResolver;
import com.tbs.sql.metadata.EntityColumnMetadata;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import com.tbs.sql.metadata.EntityTableMetadata;
import com.tbs.sql.util.SqlIdentifierUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 通用 Mapper 的 SQL 提供器，在运行时为 {@link GenericMapper} 生成增删改查语句。
 *
 * <p>核心机制是借 {@link ProviderContext#getMapperType()} 拿到当前 Mapper 接口类型，
 * 反射解析其继承 {@link GenericMapper} 时声明的泛型实参，从而在方法签名保持
 * {@code insert(T entity)} 形式的前提下得到具体实体类型。实体到表的映射由
 * {@link EntityMetadataExtractor#pullTableMetadata} 提供，并按类缓存。</p>
 *
 * <h3>方法参数约定</h3>
 * <ul>
 *   <li>所有方法接收 {@code Map<String, Object>}，键与 Mapper 方法上的 {@code @Param} 名称一致</li>
 *   <li>仅涉及主键的语句需要解析泛型，其余语句可直接从实体参数取得类型</li>
 *   <li>批量语句的参数名固定为 {@code records}，IN 列表固定为 {@code idList}</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>本类不开启 {@code useGeneratedKeys}，主键由应用侧生成，四库行为一致</li>
 *   <li>条件拼接只取非空、非空串、非集合、非数组的字段，全空时退化为无 WHERE 的全表语句</li>
 *   <li>方言实例在语句生成时从 Spring 容器获取，此时容器必然已就绪</li>
 *   <li>静态语句（全字段插入、按主键查询/删除/更新）的 SQL 文本按实体类缓存，动态语句每次执行重建</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see GenericMapper
 * @see ProviderContext
 */
public class GenericSqlProvider {

    /** 静态语句 SQL 缓存，键为“实体类全名#方法标识”，仅存放对同一实体类文本恒定的语句 */
    private static final Map<String, String> staticSqlCache = new ConcurrentHashMap<>();

    /**
     * 生成全字段单行插入语句。
     *
     * @param params 参数表，{@code entity} 为待插入实体
     * @return INSERT语句
     */
    public String insert(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        return cachedStaticSql(metadata, "insert",
            () -> buildSingleInsert(metadata, metadata.getColumns(), "entity"));
    }

    /**
     * 生成仅含非空字段的单行插入语句。
     *
     * @param params 参数表，{@code entity} 为待插入实体
     * @return INSERT语句
     */
    public String insertSelective(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        List<EntityColumnMetadata> columns = new ArrayList<>();
        for (EntityColumnMetadata column : metadata.getColumns()) {
            if (readValue(column, entity) != null) {
                columns.add(column);
            }
        }
        return buildSingleInsert(metadata, columns, "entity");
    }

    /**
     * 生成单批多行插入语句，语法与容量上限由方言决定。
     *
     * @param params 参数表，{@code records} 为单批实体列表
     * @return 批量INSERT语句
     */
    public String insertBatch(Map<String, Object> params) {
        List<?> records = (List<?>)params.get("records");
        AssertUtil.isTrue(records == null || records.isEmpty(), "批量插入的records不能为空，请使用insertList");
        EntityTableMetadata metadata = EntityMetadataExtractor.pullTableMetadata(records.get(0).getClass());
        List<String> columns = new ArrayList<>(metadata.getColumns().size());
        List<String> properties = new ArrayList<>(metadata.getColumns().size());
        for (EntityColumnMetadata column : metadata.getColumns()) {
            columns.add(SqlIdentifierUtils.column(column.getColumn()));
            properties.add(column.getProperty());
        }
        IInsertExtractor extractor = getDialect().getInsertExtractor();
        return extractor.buildBatchInsert(metadata.getTableName(), columns, properties, records.size());
    }

    /**
     * 生成按主键查询单条记录的语句。
     *
     * @param params 参数表，{@code id} 为主键值
     * @return SELECT语句
     */
    public String selectByPrimaryKey(ProviderContext context, Map<String, Object> params) {
        EntityTableMetadata metadata = resolveMetadata(context, null);
        return cachedStaticSql(metadata, "selectByPrimaryKey",
            () -> buildSelect(metadata, buildPrimaryKeyCondition(metadata)));
    }

    /**
     * 生成按主键批量查询单批记录的语句。
     *
     * @param params 参数表，{@code idList} 为单批主键值列表
     * @return SELECT语句
     */
    public String selectByIdListBatch(ProviderContext context, Map<String, Object> params) {
        List<?> idList = (List<?>)params.get("idList");
        AssertUtil.isTrue(idList == null || idList.isEmpty(), "分批查询的idList不能为空，请使用selectByIdList");
        EntityTableMetadata metadata = resolveMetadata(context, null);
        return buildSelect(metadata, buildInCondition(metadata, idList.size()));
    }

    /**
     * 生成按实体非空字段等值查询的语句。
     *
     * @param params 参数表，{@code entity} 为查询条件实体
     * @return SELECT语句
     */
    public String select(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        return buildSelect(metadata, buildEntityCondition(metadata, "entity", entity));
    }

    /**
     * 生成按实体非空字段等值统计的语句。
     *
     * @param params 参数表，{@code entity} 为查询条件实体
     * @return COUNT语句
     */
    public String selectCount(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        String condition = buildEntityCondition(metadata, "entity", entity);
        StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ").append(metadata.getTableName());
        if (!condition.isEmpty()) {
            builder.append(" WHERE ").append(condition);
        }
        return builder.toString();
    }

    /**
     * 生成按主键更新全部持久化字段的语句。
     *
     * @param params 参数表，{@code entity} 为待更新实体
     * @return UPDATE语句
     */
    public String updateByPrimaryKey(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        EntityColumnMetadata idColumn = requireIdColumn(metadata);
        AssertUtil.notNull(readValue(idColumn, entity),
            "更新" + metadata.getTableName() + "时主键" + idColumn.getProperty() + "不能为空");
        return cachedStaticSql(metadata, "updateByPrimaryKey",
            () -> buildUpdate(metadata, metadata.getColumns()));
    }

    /**
     * 生成按主键更新非空字段的语句。
     *
     * @param params 参数表，{@code entity} 为待更新实体
     * @return UPDATE语句
     */
    public String updateByPrimaryKeySelective(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        EntityColumnMetadata idColumn = requireIdColumn(metadata);
        AssertUtil.notNull(readValue(idColumn, entity),
            "更新" + metadata.getTableName() + "时主键" + idColumn.getProperty() + "不能为空");
        List<EntityColumnMetadata> columns = new ArrayList<>();
        for (EntityColumnMetadata column : metadata.getColumns()) {
            if (readValue(column, entity) != null) {
                columns.add(column);
            }
        }
        return buildUpdate(metadata, columns);
    }

    /**
     * 生成按主键删除记录的语句。
     *
     * @param params 参数表，{@code id} 为主键值
     * @return DELETE语句
     */
    public String deleteByPrimaryKey(ProviderContext context, Map<String, Object> params) {
        EntityTableMetadata metadata = resolveMetadata(context, null);
        return cachedStaticSql(metadata, "deleteByPrimaryKey", () -> getDialect().getDeleteExtractor()
            .buildDelete(metadata.getTableName(), buildPrimaryKeyCondition(metadata)));
    }

    /**
     * 生成按实体非空字段等值删除的语句。
     *
     * @param params 参数表，{@code entity} 为删除条件实体
     * @return DELETE语句
     */
    public String delete(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        String condition = buildEntityCondition(metadata, "entity", entity);
        return getDialect().getDeleteExtractor().buildDelete(metadata.getTableName(), condition);
    }

    /**
     * 生成按主键批量删除单批记录的语句。
     *
     * @param params 参数表，{@code idList} 为单批主键值列表
     * @return DELETE语句
     */
    public String deleteByIdListBatch(ProviderContext context, Map<String, Object> params) {
        List<?> idList = (List<?>)params.get("idList");
        AssertUtil.isTrue(idList == null || idList.isEmpty(), "分批删除的idList不能为空，请使用deleteByIdList");
        EntityTableMetadata metadata = resolveMetadata(context, null);
        return getDialect().getDeleteExtractor().buildDelete(metadata.getTableName(), buildInCondition(metadata, idList.size()));
    }

    /**
     * 解析实体表元数据。
     *
     * <p>实体参数存在时直接取运行时类型，否则反射解析 Mapper 接口继承 {@link GenericMapper}
     * 时声明的第一个泛型实参。仅按主键操作的语句需要后者。</p>
     *
     * @param context 提供器上下文，携带当前 Mapper 接口类型
     * @param entity  实体参数，可为null
     * @return 实体表元数据
     */
    private EntityTableMetadata resolveMetadata(ProviderContext context, Object entity) {
        Class<?> entityClass = entity != null ? entity.getClass()
            : MapperEntityTypeResolver.resolveEntityClass(context.getMapperType());
        AssertUtil.notNull(entityClass,
            "无法解析Mapper接口" + context.getMapperType().getName() + "的实体类型，请确认其继承GenericMapper或IFrameworkMapper");
        return EntityMetadataExtractor.pullTableMetadata(entityClass);
    }

    /**
     * 构建单行插入语句。
     *
     * @param metadata  实体表元数据
     * @param columns   参与插入的列
     * @param paramName 参数名，用于生成占位符前缀
     * @return INSERT语句
     */
    private String buildSingleInsert(EntityTableMetadata metadata, List<EntityColumnMetadata> columns, String paramName) {
        StringBuilder builder = new StringBuilder("INSERT INTO ").append(metadata.getTableName()).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(SqlIdentifierUtils.column(columns.get(i).getColumn()));
        }
        builder.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("#{").append(paramName).append('.').append(columns.get(i).getProperty()).append('}');
        }
        return builder.append(')').toString();
    }

    /**
     * 构建查询语句。
     *
     * <p>列名统一追加 {@code AS 属性名} 别名：{@code @Column} 指定的列名与 Java 属性名不一致时
     * （如 {@code prop_value} 对应 {@code value}），MyBatis 只能按列名自动映射，缺少别名会导致属性取不到值。</p>
     *
     * @param metadata  实体表元数据
     * @param condition WHERE条件，为空时不拼接WHERE子句
     * @return SELECT语句
     */
    private String buildSelect(EntityTableMetadata metadata, String condition) {
        StringBuilder builder = new StringBuilder("SELECT ");
        List<EntityColumnMetadata> columns = metadata.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(SqlIdentifierUtils.column(columns.get(i).getColumn()))
                .append(" AS ").append(SqlIdentifierUtils.alias(columns.get(i).getProperty()));
        }
        builder.append(" FROM ").append(metadata.getTableName());
        if (!condition.isEmpty()) {
            builder.append(" WHERE ").append(condition);
        }
        return builder.toString();
    }

    /**
     * 构建按主键更新的语句，SET 部分排除主键列：主键仅用于 WHERE 定位，
     * 重复绑定只会徒增参数，批量路径逐行复用本语句时收益更明显。
     *
     * <p>主键非空校验由调用方在每次执行时完成，本方法只负责文本生成；全字段更新的构建结果会被缓存。</p>
     *
     * @param metadata 实体表元数据
     * @param columns  参与SET的列
     * @return UPDATE语句
     */
    private String buildUpdate(EntityTableMetadata metadata, List<EntityColumnMetadata> columns) {
        EntityColumnMetadata idColumn = requireIdColumn(metadata);
        List<String> columnNames = new ArrayList<>(columns.size());
        List<String> propertyNames = new ArrayList<>(columns.size());
        for (EntityColumnMetadata column : columns) {
            if (column.isId()) {
                continue;
            }
            columnNames.add(SqlIdentifierUtils.column(column.getColumn()));
            propertyNames.add(column.getProperty());
        }
        AssertUtil.isTrue(columnNames.isEmpty(), "更新" + metadata.getTableName()
            + "时除主键外没有任何参与SET的字段，无可更新内容");
        return getDialect().getUpdateExtractor().buildUpdate(metadata.getTableName(), columnNames, propertyNames,
            SqlIdentifierUtils.column(idColumn.getColumn()), idColumn.getProperty());
    }

    /**
     * 获取静态语句的缓存 SQL，未命中时构建并写入缓存。
     *
     * <p>仅适用于对同一实体类文本恒定的语句（全字段插入、按主键查询/删除/更新）；构建过程依赖
     * Spring 容器中的方言实例，仅在首次未命中时执行，此时容器必然已就绪。依赖运行时实体字段值的
     * 动态语句不得使用本缓存。</p>
     *
     * @param metadata   实体表元数据
     * @param method     方法标识，参与缓存键
     * @param sqlBuilder 构建逻辑，仅在未命中时执行
     * @return 缓存的 SQL 文本
     */
    private String cachedStaticSql(EntityTableMetadata metadata, String method, Supplier<String> sqlBuilder) {
        return staticSqlCache.computeIfAbsent(metadata.getEntityClass().getName() + '#' + method,
            key -> sqlBuilder.get());
    }

    /**
     * 构建主键等值条件。
     *
     * @param metadata 实体表元数据
     * @return 形如 {@code id = #{id}} 的条件表达式
     */
    private String buildPrimaryKeyCondition(EntityTableMetadata metadata) {
        EntityColumnMetadata idColumn = requireIdColumn(metadata);
        return SqlIdentifierUtils.column(idColumn.getColumn()) + " = #{id}";
    }

    /**
     * 构建 IN 条件。
     *
     * @param metadata 实体表元数据
     * @param size     单批元素个数
     * @return 形如 {@code id IN (#{idList[0]}, #{idList[1]})} 的条件表达式
     */
    private String buildInCondition(EntityTableMetadata metadata, int size) {
        EntityColumnMetadata idColumn = requireIdColumn(metadata);
        StringBuilder builder = new StringBuilder(SqlIdentifierUtils.column(idColumn.getColumn())).append(" IN (");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("#{idList[").append(i).append("]}");
        }
        return builder.append(')').toString();
    }

    /**
     * 构建按实体非空字段等值匹配的条件。
     *
     * <p>集合与数组类型的字段不参与条件拼接，避免生成无法绑定的占位符；空串同样跳过，
     * 与 tk.mybatis 的 {@code whereAllIfColumns(isNotEmpty)} 语义一致，前端用空串表示“不限”时不会拼出
     * {@code column = ''} 而查不到数据。</p>
     *
     * @param metadata  实体表元数据
     * @param paramName 参数名，用于生成占位符前缀
     * @param entity    条件实体，为null时返回空条件
     * @return AND连接的条件表达式，无条件时返回空字符串
     */
    private String buildEntityCondition(EntityTableMetadata metadata, String paramName, Object entity) {
        if (entity == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (EntityColumnMetadata column : metadata.getColumns()) {
            Object value = readValue(column, entity);
            if (value == null || value instanceof Collection || value.getClass().isArray()
                || value instanceof CharSequence text && text.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append(SqlIdentifierUtils.column(column.getColumn())).append(" = #{").append(paramName)
                .append('.').append(column.getProperty()).append('}');
        }
        return builder.toString();
    }

    /**
     * 获取主键列，实体未标注主键时中断执行。
     *
     * @param metadata 实体表元数据
     * @return 主键列元数据
     */
    private EntityColumnMetadata requireIdColumn(EntityTableMetadata metadata) {
        EntityColumnMetadata idColumn = metadata.getIdColumn();
        AssertUtil.notNull(idColumn, "实体类" + metadata.getEntityClass().getName() + "没有标注@Id注解");
        return idColumn;
    }

    /**
     * 读取实体字段值。
     *
     * @param column 列元数据，其中反射字段已在元数据提取阶段开放访问权限
     * @param entity 实体对象
     * @return 字段值
     */
    private Object readValue(EntityColumnMetadata column, Object entity) {
        try {
            return column.getField().get(entity);
        } catch (IllegalAccessException e) {
            IllegalStateException error = new IllegalStateException(
                I18nUtil.get("sql.entity.field.read.failed", column.getProperty()), e);
            AssertUtil.throwError(() -> error);
            throw error;
        }
    }

    /**
     * 获取当前数据库方言。
     *
     * @return 方言实例
     */
    private SqlDialect getDialect() {
        return SpringUtil.getBean(SqlDialect.class);
    }
}
