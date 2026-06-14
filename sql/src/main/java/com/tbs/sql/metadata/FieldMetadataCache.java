package com.tbs.sql.metadata;

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
 * 字段元数据缓存管理器
 * <p>
 * 缓存实体类的字段元数据信息，避免重复反射操作
 * </p>
 *
 * @author abstergo
 * @since 2.0.0
 */
public class FieldMetadataCache {

    private static final Map<Class<?>, List<FieldMetadataEntry>> fieldMetadataCache = new ConcurrentHashMap<>(128);

    /**
     * 获取字段元数据条目
     */
    public static List<FieldMetadataEntry> getFieldMetadata(Class<?> clazz) {
        return fieldMetadataCache.computeIfAbsent(clazz, FieldMetadataCache::buildFieldMetadata);
    }

    /**
     * 获取带有查询注解的字段列表
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
            metadataList.add(new FieldMetadataEntry(field, queryFields, connectorEnum));
        }
        return metadataList;
    }

    private static int getFieldIndex(Field field) {
        final QueryIndex annotation = field.getDeclaredAnnotation(QueryIndex.class);
        return (annotation != null) ? annotation.index() : 1000;
    }

    /**
     * 字段元数据条目
     */
    public static class FieldMetadataEntry {
        private final Field field;
        private final Set<QueryField> queryFields;
        private final ConnectorEnum connectorEnum;
        private final List<QueryField> queryFieldList;

        public FieldMetadataEntry(Field field, Set<QueryField> queryFields, ConnectorEnum connectorEnum) {
            this.field = field;
            this.queryFields = queryFields;
            this.connectorEnum = connectorEnum;
            this.queryFieldList = queryFields.stream()
                .sorted(Comparator.comparingInt(QueryField::index))
                .collect(Collectors.toList());
        }

        public Field getField() { return field; }
        public ConnectorEnum getConnectorEnum() { return connectorEnum; }
        public List<QueryField> getQueryFields() { return queryFieldList; }
    }
}
