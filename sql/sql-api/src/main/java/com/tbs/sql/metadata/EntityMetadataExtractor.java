package com.tbs.sql.metadata;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.annotation.core.SelectSQL;
import com.tbs.sql.annotation.core.SqlEntityScan;
import com.tbs.sql.annotation.mapping.SqlJdbcType;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;
import com.tbs.sql.util.SqlUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据提取器类，负责从实体类中提取SQL查询元数据。
 *
 * <p>该类提供了从实体类中提取表名、字段名等元数据信息的功能，
 * 用于构建SELECT语句。支持缓存机制，避免重复提取。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从实体类中提取表名和字段信息</li>
 *   <li>从实体类中提取写操作所需的表元数据</li>
 *   <li>支持类级 {@link SelectSQL} 注解直接指定 SELECT 语句</li>
 *   <li>支持 {@link Table} 注解的表名提取与 {@link Id} 注解的主键识别</li>
 *   <li>缓存已提取的元数据，提高性能</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL构建过程中提取实体元数据</li>
 *   <li>构建SELECT语句的列信息</li>
 *   <li>获取表名和字段映射关系</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 提取实体元数据
 * SelectInfoModel selectInfo = EntityMetadataExtractor.pullFromClass(User.class);
 * String tableName = selectInfo.getTableName();
 * List<SelectItemModel> fields = selectInfo.getFieldList();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实体类必须添加 {@link Table} 注解，标注了类级 {@link SelectSQL} 的查询模型除外</li>
 *   <li>父类声明的字段同样参与元数据提取，与 jkit-sql-auto 的实体字段扫描范围一致</li>
 *   <li>{@link SelectSQL} 走原始 SQL 通道：不产出字段清单，列投影对其无效，WHERE 仍由框架追加</li>
 *   <li>元数据会被缓存，修改实体类后需要清除缓存</li>
 *   <li>标注 {@link SqlEntityScan} 可在启动期扫描实体包、校验注解完整性，
 *       预热写操作表元数据（见 {@link #register(Class)}），并由
 *       {@link EntityTableMetadataInitializer} 预热按类缓存的 {@code SelectInfoModel}
 *       与 SELECT 片段缓存</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SelectInfoModel
 * @see SelectItemModel
 * @see EntityTableMetadata
 */
public class EntityMetadataExtractor {
    private static Map<Class<?>, SelectInfoModel> cache = new ConcurrentHashMap<>(32);
    private static final Map<Class<?>, EntityTableMetadata> tableMetadataCache = new ConcurrentHashMap<>(32);

    /**
     * 清除指定实体类的缓存元数据。
     *
     * @param entityClass 实体类类型，如果为null则不执行任何操作
     */
    public static void clearCache(Class<?> entityClass) {
        if (entityClass != null) {
            cache.remove(entityClass);
            tableMetadataCache.remove(entityClass);
        }
    }

    /**
     * 清除所有缓存的元数据。
     */
    public static void clearCache() {
        cache.clear();
        tableMetadataCache.clear();
    }

    /**
     * 从实体类中提取SQL查询元数据。
     *
     * <p>类上标注 {@link SelectSQL} 时只回填 {@code rawSql} 并提前返回：既不校验 {@link Table}，
     * 也不产出字段清单，因此该模式下列投影（include/exclude）不生效，而 WHERE、ORDER BY、LIMIT
     * 仍由框架追加，注解中的 SQL 不应自带 WHERE 子句。</p>
     *
     * @param c 实体类类型，不能为空
     * @return SELECT信息模型，包含表名、字段列表等信息；命中类级 {@link SelectSQL} 时仅含 {@code rawSql}
     */
    public static SelectInfoModel pullFromClass(Class<?> c) {
        AssertUtil.notNull(c, "根据实体类型提取查询语句，实体类型不能为空");
        SelectInfoModel model = cache.computeIfAbsent(c, entityClass -> {
            SelectInfoModel selectInfoModel = new SelectInfoModel();
            /*
             * 自定义 SQL 优先于元数据推导：注解只认类自身声明，父类上的 @SelectSQL 对子类不生效。
             */
            SelectSQL selectSQL = entityClass.getDeclaredAnnotation(SelectSQL.class);
            if (selectSQL != null && StrUtil.isNotEmpty(selectSQL.value())) {
                selectInfoModel.setRawSql(selectSQL.value());
                return selectInfoModel;
            }

            Table table = entityClass.getDeclaredAnnotation(Table.class);
            AssertUtil.notNull(table, "实体类" + entityClass.getName() + "没有添加@Table注解");
            selectInfoModel.setTableName(table.name());
            List<SelectItemModel> items = new LinkedList<>();
            for (Field field : SqlUtils.getPersistentFields(entityClass)) {
                SelectItemModel item =
                    new SelectItemModel(SqlUtils.getFieldNameWithMapping(field), field.getName());
                items.add(item);
            }
            selectInfoModel.setEntityClass(entityClass);
            selectInfoModel.setFieldList(items);
            return selectInfoModel;
        });
        // 本对象是按实体类长期缓存、结构不再变化的实例：允许提取器把已渲染的
        // SELECT 片段挂在实例上跨查询复用。EntitySqlDriver 与 IQueryCriteria
        // 的实体元数据实现都会拿到这同一个对象，两条主路径均可命中。
        model.markSelectCacheable();
        return model;
    }

    /**
     * 从实体类中提取写操作所需的表元数据。
     *
     * <p>与 {@link #pullFromClass} 的区别：该方法识别 {@link Id} 主键、保留字段声明顺序，
     * 并预先开放反射访问权限，供 INSERT / UPDATE / DELETE 语句生成与实体取值使用。</p>
     *
     * @param entityClass 实体类类型，不能为空
     * @return 表元数据，包含表名、列清单与主键信息
     */
    public static EntityTableMetadata pullTableMetadata(Class<?> entityClass) {
        AssertUtil.notNull(entityClass, "根据实体类型提取表元数据，实体类型不能为空");
        return tableMetadataCache.computeIfAbsent(entityClass, c -> {
            List<EntityColumnMetadata> columns = new LinkedList<>();
            for (Field field : SqlUtils.getPersistentFields(c)) {
                field.setAccessible(true);
                SqlJdbcType jdbcType = field.getAnnotation(SqlJdbcType.class);
                columns.add(new EntityColumnMetadata(field.getName(), SqlUtils.getFieldNameWithMapping(field),
                    field.isAnnotationPresent(Id.class), field, jdbcType == null ? null : jdbcType.value()));
            }
            return new EntityTableMetadata(SqlUtils.getTableName(c), c, columns);
        });
    }

    /**
     * 校验实体注解完整性，并把写操作表元数据写入缓存（启动期预热）。
     *
     * <p>由 {@link SqlEntityScan} 在容器刷新阶段对每个候选实体调用。
     * 校验口径与运行期 SQL 生成保持一致：{@code @Table} 只认类自身直接标注（与
     * {@code SqlUtils#getTableName} 相同），主键数量按参与持久化的字段统计，因此把 {@code @Id}
     * 标在 {@code static}、{@code final} 或 {@code @Transient} 字段上会被判为"缺少主键"。</p>
     *
     * <p>校验不通过时抛出携带具体原因的运行时异常，由初始化器汇总后中断启动；
     * 校验通过后调用 {@link #pullTableMetadata(Class)}，后续写 SQL 直接命中缓存。
     * 该方法不改动惰性加载路径，未启用扫描注解时框架行为不变。</p>
     *
     * <p>读 SQL 侧的 {@code SelectInfoModel} 与 SELECT 片段缓存不在本方法内预热，
     * 由 {@link EntityTableMetadataInitializer} 在全部实体校验通过后统一处理。</p>
     *
     * @param entityClass 待校验并预热的实体类
     * @return 已缓存的实体表元数据
     * @see SqlEntityScan
     */
    public static EntityTableMetadata register(Class<?> entityClass) {
        AssertUtil.notNull(entityClass, "注册实体表元数据，实体类型不能为空");
        String className = entityClass.getName();
        AssertUtil.isTrue(!entityClass.isAnnotationPresent(Entity.class),
            "实体类" + className + "缺少@jakarta.persistence.Entity注解");
        Table table = entityClass.getAnnotation(Table.class);
        AssertUtil.notNull(table, "实体类" + className + "缺少@jakarta.persistence.Table注解");
        AssertUtil.isTrue(StrUtil.isEmpty(table.name()), "实体类" + className + "的@Table未声明name");
        AssertUtil.isTrue(entityClass.isInterface() || entityClass.isEnum()
                || Modifier.isAbstract(entityClass.getModifiers()),
            "实体类" + className + "不能是接口、枚举或抽象类");

        EntityTableMetadata metadata = pullTableMetadata(entityClass);
        List<EntityColumnMetadata> columns = metadata.getColumns();
        AssertUtil.isTrue(columns == null || columns.isEmpty(), "实体类" + className + "没有可持久化字段");
        long idCount = columns.stream().filter(EntityColumnMetadata::isId).count();
        AssertUtil.isTrue(idCount != 1,
            "实体类" + className + "必须且只能标注一个@Id主键，实际" + idCount + "个");
        Set<String> seenColumns = new HashSet<>(columns.size());
        Set<String> duplicatedColumns = new HashSet<>();
        for (EntityColumnMetadata column : columns) {
            if (!seenColumns.add(column.getColumn())) {
                duplicatedColumns.add(column.getColumn());
            }
        }
        AssertUtil.isTrue(!duplicatedColumns.isEmpty(),
            "实体类" + className + "存在重复列名：" + duplicatedColumns);
        return metadata;
    }
}
