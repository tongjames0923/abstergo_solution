package com.tbs.sql.util;

import com.tbs.common.util.base.ReflectUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.annotation.mapping.QueryMapping;
import jakarta.persistence.Column;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
 *   <li>收集实体及其父类的持久化字段 {@link #getPersistentFields(Class)}</li>
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
 *   <li>字段映射优先级：@Column > @QueryMapping > 驼峰转下划线列名</li>
 *   <li>父类声明的字段同样参与持久化，与 Hibernate 的映射范围一致</li>
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
     *
     * <p>优先级：@Column > @QueryMapping > 驼峰转下划线。未显式声明列名时按 Hibernate 的
     * 物理命名策略转换，保证运行期 SQL 的列名与 {@code ddl-auto} 建出的表结构一致。</p>
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
            return camelToUnderscore(f.getName());
        });
    }

    /**
     * 将驼峰字段名转换为下划线列名。
     *
     * <p>规则与 Hibernate 的 {@code CamelCaseToUnderscoresNamingStrategy} 保持一致：
     * 仅在“小写字母 + 大写字母 + 小写字母”的交界处插入下划线，因此 {@code createTime}
     * 转换为 {@code create_time}，而连续大写的 {@code userID} 转换为 {@code userid}。
     * 这样实体未声明 {@code @Column} 时，运行期列名与建表列名不会出现偏差。</p>
     *
     * @param name 驼峰形式的字段名
     * @return 下划线形式的列名
     */
    private static String camelToUnderscore(String name) {
        StringBuilder builder = new StringBuilder(name);
        for (int i = 1; i < builder.length() - 1; i++) {
            if (Character.isLowerCase(builder.charAt(i - 1)) && Character.isUpperCase(builder.charAt(i))
                && Character.isLowerCase(builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return builder.toString().toLowerCase(Locale.ROOT);
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
     * 收集实体类及其父类声明的持久化字段。
     *
     * <p>映射范围与 Hibernate 保持一致：父类声明的字段同样持久化。同名属性只保留子类声明，
     * 避免子类覆写父类字段时生成重复列。静态字段、final 字段与 {@code @Transient} 字段不参与。</p>
     *
     * @param clazz 实体类
     * @return 持久化字段列表，子类字段在前
     */
    public static List<Field> getPersistentFields(Class<?> clazz) {
        List<Field> fields = new LinkedList<>();
        Set<String> names = new HashSet<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (isSkipField(field) || !names.add(field.getName())) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * 获取类对应的表名。
     *
     * @param clazz 实体类
     * @return 表名，如果类为null则返回null；没有@Table注解时返回类名按Hibernate规则转下划线后的结果
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
            return camelToUnderscore(c.getSimpleName());
        });
    }
}
