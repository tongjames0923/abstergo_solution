package com.tbs.sql.metadata;

import com.tbs.sql.annotation.condition.OrField;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.condition.QueryFields;
import com.tbs.sql.annotation.condition.QueryIndex;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.enums.ConnectorEnum;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 字段元数据缓存管理器类，用于缓存和管理实体类的字段元数据信息。
 *
 * <p>该类提供了从实体类中提取和缓存字段元数据信息的功能，包括查询字段、排序字段、
 * 连接符等信息。通过缓存机制避免重复的反射操作，提高性能。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>提取和缓存字段元数据 {@link #getFieldMetadata(Class)}</li>
 *   <li>收集带注解的字段列表 {@link #collectAnnotatedFields(Class)}</li>
 *   <li>缓存机制，避免重复反射</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在查询条件构建过程中获取字段元数据</li>
 *   <li>避免重复的反射操作</li>
 *   <li>管理字段的查询配置信息</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 获取字段元数据
 * List<FieldMetadataEntry> metadata = FieldMetadataCache.getFieldMetadata(UserQuery.class);
 *
 * // 收集带注解的字段
 * List<Field> fields = FieldMetadataCache.collectAnnotatedFields(UserQuery.class);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>缓存是静态的，所有实例共享</li>
 *   <li>修改字段配置后需要清除缓存</li>
 *   <li>线程安全</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see FieldMetadataEntry
 * @see com.tbs.sql.annotation.condition.QueryField
 */
public class FieldMetadataCache {

    private static final Map<Class<?>, List<FieldMetadataEntry>> fieldMetadataCache = new ConcurrentHashMap<>(128);

    /**
     * 获取字段元数据条目列表。
     *
     * @param clazz 实体类或查询条件类
     * @return 字段元数据条目列表
     */
    public static List<FieldMetadataEntry> getFieldMetadata(Class<?> clazz) {
        return fieldMetadataCache.computeIfAbsent(clazz, FieldMetadataCache::buildFieldMetadata);
    }

    /**
     * 获取带有查询注解的字段列表。
     *
     * @param clazz 实体类或查询条件类
     * @return 带有查询注解的字段列表，已按索引排序
     */
    public static List<Field> collectAnnotatedFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(
                field -> field.isAnnotationPresent(QueryIndex.class) ||
                    field.isAnnotationPresent(QueryField.class) ||
                    field.isAnnotationPresent(QueryOrderField.class) ||
                    AnnotatedElementUtils.hasAnnotation(field, QueryFields.class))
            .sorted(Comparator.comparingInt(FieldMetadataCache::getFieldIndex))
            .collect(Collectors.toList());
    }

    private static List<FieldMetadataEntry> buildFieldMetadata(Class<?> clazz) {
        List<FieldMetadataEntry> metadataList = new ArrayList<>(32);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Set<QueryField> queryFields = AnnotatedElementUtils.getAllMergedAnnotations(field, QueryField.class);
            if (queryFields == null || queryFields.isEmpty()) {
                continue;
            }
            ConnectorEnum connectorEnum = !AnnotatedElementUtils.hasAnnotation(field,
                    com.tbs.sql.annotation.condition.OrField.class)
                ? ConnectorEnum.AND
                : ConnectorEnum.OR;
            // 缓存的Field对象会被多线程并发读取，在构建缓存时一次性设置为可访问且不再重置，
            // 避免并发场景下反复setAccessible(true/false)导致的IllegalAccessException竞态问题
            field.setAccessible(true);
            metadataList.add(new FieldMetadataEntry(field, queryFields, connectorEnum));
        }
        return metadataList;
    }

    private static int getFieldIndex(Field field) {
        final QueryIndex annotation = field.getDeclaredAnnotation(QueryIndex.class);
        return (annotation != null) ? annotation.index() : 1000;
    }

    /**
     * 字段元数据条目类，存储单个字段的元数据信息。
     *
     * <p>该类封装了字段的元数据信息，包括字段对象、查询字段注解集合、
     * 连接符类型等。用于在查询条件构建过程中传递字段配置信息。</p>
     *
     * <h3>主要功能</h3>
     * <ul>
     *   <li>存储字段对象 {@link #getField()}</li>
     *   <li>存储查询字段注解集合 {@link #getQueryFields()}</li>
     *   <li>存储连接符类型 {@link #getConnectorEnum()}</li>
     * </ul>
     *
     * <h3>使用场景</h3>
     * <ul>
     *   <li>在查询条件构建过程中传递字段配置</li>
     *   <li>缓存字段的元数据信息</li>
     *   <li>管理字段的查询配置</li>
     * </ul>
     *
     * <h3>注意事项</h3>
     * <ul>
     *   <li>查询字段列表已按索引排序</li>
     *   <li>连接符类型由 {@link OrField} 注解决定</li>
     * </ul>
     *
     * @author abstergo
     * @since 2.0.0
     * @see FieldMetadataCache
     */
    public static class FieldMetadataEntry {
        private final Field field;
        private final Set<QueryField> queryFields;
        private final ConnectorEnum connectorEnum;
        private final List<QueryField> queryFieldList;

        /**
         * 构造函数，初始化字段元数据条目。
         *
         * @param field 字段对象
         * @param queryFields 查询字段注解集合
         * @param connectorEnum 连接符类型（AND或OR）
         */
        public FieldMetadataEntry(Field field, Set<QueryField> queryFields, ConnectorEnum connectorEnum) {
            this.field = field;
            this.queryFields = queryFields;
            this.connectorEnum = connectorEnum;
            this.queryFieldList = queryFields.stream()
                .sorted(Comparator.comparingInt(QueryField::index))
                .collect(Collectors.toList());
        }

        /**
         * 获取字段对象。
         *
         * @return 字段对象
         */
        public Field getField() { return field; }

        /**
         * 获取连接符类型。
         *
         * @return 连接符类型（AND或OR）
         */
        public ConnectorEnum getConnectorEnum() { return connectorEnum; }

        /**
         * 获取查询字段列表（已按索引排序）。
         *
         * @return 查询字段列表
         */
        public List<QueryField> getQueryFields() { return queryFieldList; }
    }
}
