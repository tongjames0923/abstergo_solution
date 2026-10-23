package com.tbs.sql.mapper.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.dialect.IInsertExtractor;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.mapper.GenericMapper;
import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.sql.metadata.EntityColumnMetadata;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import com.tbs.sql.metadata.EntityTableMetadata;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see GenericMapper
 * @see ProviderContext
 */
public class GenericSqlProvider {

    /**
     * 生成全字段单行插入语句。
     *
     * @param params 参数表，{@code entity} 为待插入实体
     * @return INSERT语句
     */
    public String insert(ProviderContext context, Map<String, Object> params) {
        Object entity = params.get("entity");
        EntityTableMetadata metadata = resolveMetadata(context, entity);
        return buildSingleInsert(metadata, metadata.getColumns(), "entity");
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
            columns.add(column.getColumn());
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
        return buildSelect(metadata, buildPrimaryKeyCondition(metadata));
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
        return buildUpdate(metadata, metadata.getColumns(), entity);
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
        List<EntityColumnMetadata> columns = new ArrayList<>();
        for (EntityColumnMetadata column : metadata.getColumns()) {
            if (readValue(column, entity) != null) {
                columns.add(column);
            }
        }
        return buildUpdate(metadata, columns, entity);
    }

    /**
     * 生成按主键删除记录的语句。
     *
     * @param params 参数表，{@code id} 为主键值
     * @return DELETE语句
     */
    public String deleteByPrimaryKey(ProviderContext context, Map<String, Object> params) {
        EntityTableMetadata metadata = resolveMetadata(context, null);
        return "DELETE FROM " + metadata.getTableName() + " WHERE " + buildPrimaryKeyCondition(metadata);
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
        String sql = "DELETE FROM " + metadata.getTableName();
        return condition.isEmpty() ? sql : sql + " WHERE " + condition;
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
        return "DELETE FROM " + metadata.getTableName() + " WHERE " + buildInCondition(metadata, idList.size());
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
        Class<?> entityClass = entity != null ? entity.getClass() : resolveEntityClass(context.getMapperType());
        AssertUtil.notNull(entityClass,
            "无法解析Mapper接口" + context.getMapperType().getName() + "的实体类型，请确认其继承GenericMapper或IFrameworkMapper");
        return EntityMetadataExtractor.pullTableMetadata(entityClass);
    }

    /**
     * 解析 Mapper 接口声明的实体类型。
     *
     * <p>沿泛型父接口递归向下查找，逐层绑定类型变量，因此 {@code XxxMapper extends BaseMapper<Xxx>}
     * 这类中间抽象层也能解析出实体类型。</p>
     *
     * @param mapperType 当前 Mapper 接口类型
     * @return 实体类型，未按约定声明泛型时返回null
     */
    private Class<?> resolveEntityClass(Class<?> mapperType) {
        for (Type type : mapperType.getGenericInterfaces()) {
            Class<?> entityClass = resolveEntityClass(type, Collections.emptyMap());
            if (entityClass != null) {
                return entityClass;
            }
        }
        return null;
    }

    /**
     * 在给定类型变量绑定关系下递归解析实体类型。
     *
     * @param type     待解析的父接口类型
     * @param bindings 外层接口已绑定的类型变量
     * @return 实体类型，无法解析时返回null
     */
    private Class<?> resolveEntityClass(Type type, Map<TypeVariable<?>, Type> bindings) {
        if (!(type instanceof ParameterizedType parameterized)) {
            return null;
        }
        Class<?> raw = (Class<?>)parameterized.getRawType();
        TypeVariable<?>[] variables = raw.getTypeParameters();
        Type[] arguments = parameterized.getActualTypeArguments();
        Map<TypeVariable<?>, Type> scope = new HashMap<>(bindings);
        for (int i = 0; i < variables.length && i < arguments.length; i++) {
            Type argument = arguments[i];
            scope.put(variables[i],
                argument instanceof TypeVariable<?> variable && bindings.containsKey(variable) ? bindings.get(variable)
                    : argument);
        }
        if (raw == IFrameworkMapper.class || raw == GenericMapper.class) {
            Type entityType = scope.get(variables[0]);
            return entityType instanceof Class<?> entityClass ? entityClass : null;
        }
        for (Type parent : raw.getGenericInterfaces()) {
            Class<?> entityClass = resolveEntityClass(parent, scope);
            if (entityClass != null) {
                return entityClass;
            }
        }
        return null;
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
            builder.append(columns.get(i).getColumn());
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
            builder.append(columns.get(i).getColumn()).append(" AS ").append(columns.get(i).getProperty());
        }
        builder.append(" FROM ").append(metadata.getTableName());
        if (!condition.isEmpty()) {
            builder.append(" WHERE ").append(condition);
        }
        return builder.toString();
    }

    /**
     * 构建按主键更新的语句。
     *
     * @param metadata 实体表元数据
     * @param columns  参与SET的列
     * @param entity   待更新实体，用于校验主键非空
     * @return UPDATE语句
     */
    private String buildUpdate(EntityTableMetadata metadata, List<EntityColumnMetadata> columns, Object entity) {
        EntityColumnMetadata idColumn = requireIdColumn(metadata);
        AssertUtil.notNull(readValue(idColumn, entity),
            "更新" + metadata.getTableName() + "时主键" + idColumn.getProperty() + "不能为空");
        StringBuilder builder = new StringBuilder("UPDATE ").append(metadata.getTableName()).append(" SET ");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(columns.get(i).getColumn()).append(" = #{entity.")
                .append(columns.get(i).getProperty()).append('}');
        }
        builder.append(" WHERE ").append(idColumn.getColumn()).append(" = #{entity.")
            .append(idColumn.getProperty()).append('}');
        return builder.toString();
    }

    /**
     * 构建主键等值条件。
     *
     * @param metadata 实体表元数据
     * @return 形如 {@code id = #{id}} 的条件表达式
     */
    private String buildPrimaryKeyCondition(EntityTableMetadata metadata) {
        EntityColumnMetadata idColumn = requireIdColumn(metadata);
        return idColumn.getColumn() + " = #{id}";
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
        StringBuilder builder = new StringBuilder(idColumn.getColumn()).append(" IN (");
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
            builder.append(column.getColumn()).append(" = #{").append(paramName).append('.')
                .append(column.getProperty()).append('}');
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
            throw new IllegalStateException("读取实体字段失败：" + column.getProperty(), e);
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
