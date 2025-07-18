package com.tbs.sql.model;

import com.tbs.sql.enums.QueryContrastEnum;
import lombok.Data;


import java.io.Serializable;

/**
 * SQL  WHERE条件信息
 *
 * @author tongj
 */
@Data
public class SqlWhereInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 字段名
     */
    private String fieldName;
    /**
     * 字段值，原始值
     */
    private Object fieldValue;
    /**
     * 字段值，转换后的值，若存在则优先使用该值，否则使用fieldValue，优先度中，低于rawSql
     */
    private String fieldValueStr;
    /**
     * 字段值类型
     */
    private Class valueType;
    /**
     * 字段对比方式
     */
    private QueryContrastEnum contrast;
    /**
     * 字段所属表名
     */
    private String tableName;

    /**
     * 原始SQL，若存在则直接使用该字段，优先级高
     */
    private String rawSql;

    /**
     * 是否使用安全模式，安全模式将使用？占位符而不是直接使用字段值
     */
    private Boolean useSafe;
}
