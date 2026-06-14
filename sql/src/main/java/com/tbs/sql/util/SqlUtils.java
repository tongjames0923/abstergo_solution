package com.tbs.sql.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;

import com.tbs.common.util.base.ReflectUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.annotation.mapping.QueryMapping;

/**
 * SQL工具类，提供数据库相关操作的工具方法
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SqlUtils {
    private static final Map<Class<?>, String> tableNames = new ConcurrentHashMap<>();

    /**
     * 获取字段名，支持从@Column和@QueryMapping注解中获取映射名称
     * <p>优先级：@QueryMapping > @Column > 字段名</p>
     */
    public static String getFieldNameWithMapping(Field field) {
        if (field == null) {
            return null;
        }
        Column column = field.getDeclaredAnnotation(Column.class);
        if (column != null && StrUtil.isNotEmpty(column.name())) {
            return column.name();
        }
        QueryMapping queryMapping = field.getDeclaredAnnotation(QueryMapping.class);
        if (queryMapping != null && StrUtil.isNotEmpty(queryMapping.value())) {
            return queryMapping.value();
        }
        return field.getName();
    }

    /**
     * 判断是否应该跳过该字段
     */
    public static boolean isSkipField(Field field) {
        return field == null || ReflectUtil.isStaticOrFinal(field)
                || field.isAnnotationPresent(javax.persistence.Transient.class);
    }

    /**
     * 获取类对应的表名
     */
    public static String getTableName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return tableNames.computeIfAbsent(clazz, c -> {
            if (c.isAnnotationPresent(javax.persistence.Table.class)) {
                javax.persistence.Table table = c.getAnnotation(javax.persistence.Table.class);
                if (StrUtil.isNotEmpty(table.name())) {
                    return table.name();
                }
            }
            return c.getSimpleName();
        });
    }
}
