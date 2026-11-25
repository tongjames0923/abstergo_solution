package com.tbs.sql.util;

import com.tbs.sql.metadata.EntityColumnMetadata;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * MyBatis 占位符的 jdbcType 渲染工具。
 *
 * <p>SQL Server 下未声明 jdbcType 的 null 参数会被驱动绑定为 varbinary，写入 datetime2 列时报
 * 隐式转换错误。字段标注 {@link com.tbs.sql.annotation.mapping.SqlJdbcType} 时以声明值为准，
 * 未标注时只对框架约定的时间类型推导 TIMESTAMP，其余类型保持原样。</p>
 *
 * @author abstergo
 * @since 3.0.0
 */
public final class SqlJdbcTypeUtils {

    private SqlJdbcTypeUtils() {
    }

    /**
     * 按列元数据生成占位符属性片段。
     *
     * @param property 实体属性名
     * @param column   实体列元数据
     * @return 需要声明时返回 {@code property,jdbcType=XXX}，否则原样返回
     */
    public static String withJdbcType(String property, EntityColumnMetadata column) {
        JdbcType jdbcType = column.getJdbcType();
        if (jdbcType == null) {
            Field field = column.getField();
            if (field == null) {
                return property;
            }
            Class<?> javaType = field.getType();
            jdbcType = Date.class.isAssignableFrom(javaType) || javaType == LocalDateTime.class
                ? JdbcType.TIMESTAMP : null;
        }
        if (jdbcType == null || jdbcType == JdbcType.UNDEFINED) {
            return property;
        }
        return property + ",jdbcType=" + jdbcType.name();
    }
}
