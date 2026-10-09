package com.tbs.sql.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.Column;

import com.tbs.common.util.base.ReflectUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.annotation.mapping.QueryMapping;

/**
 * SQL工具类，提供数据库相关操作的工具方法。
 *
 * <p>该类提供了字段名映射、字段跳过判断、表名获取等工具方法，
 * 用于在SQL构建过程中处理实体类的元数据信息。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取字段映射名称 {@link #getFieldNameWithMapping(Field)}</li>
 *   <li>判断是否跳过字段 {@link #isSkipField(Field)}</li>
 *   <li>获取表名 {@link #getTableName(Class)}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL构建过程中处理字段映射</li>
 *   <li>判断字段是否需要忽略</li>
 *   <li>获取实体类对应的表名</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 获取字段映射名称
 * String fieldName = SqlUtils.getFieldNameWithMapping(field);
 * 
 * // 判断是否跳过字段
 * boolean skip = SqlUtils.isSkipField(field);
 * 
 * // 获取表名
 * String tableName = SqlUtils.getTableName(User.class);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>字段映射优先级：@QueryMapping > @Column > 字段名</li>
 *   <li>静态字段和final字段会被跳过</li>
 *   <li>表名会被缓存</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.annotation.mapping.QueryMapping
 * @see jakarta.persistence.Column
 */
public class SqlUtils {
    private static final Map<Class<?>, String> tableNames = new ConcurrentHashMap<>();
    private static final Map<Field, String> fieldNameCache = new ConcurrentHashMap<>(256);
    private static final Map<Field, Boolean> skipFieldCache = new ConcurrentHashMap<>(256);

    /**
     * 获取字段名，支持从@Column和@QueryMapping注解中获取映射名称。
     * <p>优先级：@QueryMapping > @Column > 字段名</p>
     *
     * @param field 字段对象
     * @return 映射后的字段名，如果字段为null则返回null
     */
    public static String getFieldNameWithMapping(Field field) {
        if (field == null) {
            return null;
        }
        return fieldNameCache.computeIfAbsent(field, f -> {
            Column column = f.getDeclaredAnnotation(Column.class);
            if (column != null && StrUtil.isNotEmpty(column.name())) {
                return column.name();
            }
            QueryMapping queryMapping = f.getDeclaredAnnotation(QueryMapping.class);
            if (queryMapping != null && StrUtil.isNotEmpty(queryMapping.value())) {
                return queryMapping.value();
            }
            return f.getName();
        });
    }

    /**
     * 判断是否应该跳过该字段。
     *
     * @param field 字段对象
     * @return 如果应该跳过返回true，否则返回false
     */
    public static boolean isSkipField(Field field) {
        if (field == null) {
            return true;
        }
        return skipFieldCache.computeIfAbsent(field, f -> 
            ReflectUtil.isStaticOrFinal(f) || f.isAnnotationPresent(jakarta.persistence.Transient.class)
        );
    }

    /**
     * 获取类对应的表名。
     *
     * @param clazz 实体类
     * @return 表名，如果类为null则返回null，如果没有@Table注解则返回类名
     */
    public static String getTableName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return tableNames.computeIfAbsent(clazz, c -> {
            if (c.isAnnotationPresent(jakarta.persistence.Table.class)) {
                jakarta.persistence.Table table = c.getAnnotation(jakarta.persistence.Table.class);
                if (StrUtil.isNotEmpty(table.name())) {
                    return table.name();
                }
            }
            return c.getSimpleName();
        });
    }
}
