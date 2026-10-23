package com.tbs.sql.metadata;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.annotation.core.SelectSQL;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;
import com.tbs.sql.util.SqlUtils;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
 *   <li>父类声明的字段同样参与元数据提取，与 Hibernate 的映射范围一致</li>
 *   <li>{@link SelectSQL} 走原始 SQL 通道：不产出字段清单，列投影对其无效，WHERE 仍由框架追加</li>
 *   <li>元数据会被缓存，修改实体类后需要清除缓存</li>
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
        return cache.computeIfAbsent(c, entityClass -> {
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
                columns.add(new EntityColumnMetadata(field.getName(), SqlUtils.getFieldNameWithMapping(field),
                    field.isAnnotationPresent(Id.class), field));
            }
            return new EntityTableMetadata(SqlUtils.getTableName(c), c, columns);
        });
    }
}
