package com.tbs.sql.framework.model;

import com.tbs.sql.enums.QueryContrastEnum;
import lombok.Data;


import java.io.Serializable;

/**
 * SQL WHERE条件信息模型
 * <p>
 * 封装SQL WHERE条件中的字段信息，包括字段名、字段值、对比方式等
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
public class SqlWhereInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 字段名
     * <p>SQL WHERE条件中的字段名称</p>
     */
    private String fieldName;
    /**
     * 字段值，原始值
     * <p>SQL WHERE条件中的原始字段值</p>
     */
    private Object fieldValue;
    /**
     * 字段值，转换后的值
     * <p>转换后的字段值，若存在则优先使用该值，否则使用fieldValue，优先度中，低于rawSql</p>
     */
    private String fieldValueStr;
    /**
     * 字段值类型
     * <p>字段值的类型</p>
     */
    private Class valueType;
    /**
     * 字段对比方式
     * <p>SQL WHERE条件中的字段对比方式</p>
     */
    private QueryContrastEnum contrast;
    /**
     * 字段所属表名
     * <p>字段所属的表名</p>
     */
    private String tableName;

    /**
     * 原始SQL
     * <p>原始SQL条件，若存在则直接使用该字段，优先级高</p>
     */
    private String rawSql;

    /**
     * 是否使用安全模式
     * <p>安全模式将使用？占位符而不是直接使用字段值</p>
     */
    private Boolean useSafe;
}
