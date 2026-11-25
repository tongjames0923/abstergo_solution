package com.tbs.sql.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Field;

/**
 * 实体列元数据，描述实体属性与数据库列的映射关系。
 *
 * <p>供写操作（INSERT / UPDATE / DELETE）SQL 生成使用，包含 Java 属性名、数据库列名、
 * 主键标记以及用于读取实体运行时值的反射字段。</p>
 *
 * <h3>能力边界</h3>
 * <ul>
 *   <li>只描述可持久化字段，{@code @Transient} 与静态、final 字段已在提取阶段排除</li>
 *   <li>列名优先级沿用 {@link com.tbs.sql.util.SqlUtils#getFieldNameWithMapping} 的规则</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see EntityTableMetadata
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityColumnMetadata {
    /** Java 属性名 */
    private String property;
    /** 数据库列名 */
    private String column;
    /** 是否主键 */
    private boolean id;
    /** 反射字段，用于读取实体运行时值 */
    private Field field;
    /** 字段显式声明的 JDBC 类型，未标注 @SqlJdbcType 时为 null */
    private JdbcType jdbcType;

    /**
     * 兼容未声明 JDBC 类型的构造入口。
     *
     * @param property  Java 属性名
     * @param column    数据库列名
     * @param id        是否主键
     * @param field     反射字段
     */
    public EntityColumnMetadata(String property, String column, boolean id, Field field) {
        this(property, column, id, field, null);
    }
}
