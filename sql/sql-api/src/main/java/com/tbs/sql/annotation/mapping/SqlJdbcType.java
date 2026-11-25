package com.tbs.sql.annotation.mapping;

import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.*;

/**
 * 实体字段的 JDBC 类型声明。
 *
 * <p>显式指定后优先于按 Java 类型的自动推断，用于生成 MyBatis 占位符的 {@code jdbcType} 片段。
 * 典型场景是 SQL Server 的 datetime2 列：null 参数未声明 jdbcType 时会被绑定为 varbinary， 从而触发隐式转换错误。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @SqlJdbcType(JdbcType.TIMESTAMP)
 * @Column(name = "expire_time")
 * private Date expireTime;
 *
 * // UNDEFINED 表示显式关闭该字段的 jdbcType 片段
 * @SqlJdbcType(JdbcType.UNDEFINED)
 * private String payload;
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>只影响框架生成的 INSERT / UPDATE 占位符，手写 SQL 仍需自行声明</li>
 *   <li>未标注时按 Java 类型推断：{@code Date} 与 {@code LocalDateTime} 使用 TIMESTAMP</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SqlJdbcType {

    /**
     * JDBC 类型。
     *
     * @return {@link JdbcType#UNDEFINED} 表示不生成 jdbcType 片段
     */
    JdbcType value() default JdbcType.UNDEFINED;
}
