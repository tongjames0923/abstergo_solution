package com.tbs.sql.model.where;

import com.tbs.sql.enums.ContrastEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * SQL WHERE条件信息模型
 * <p>
 * 封装SQL WHERE条件中的字段信息，包括字段名、字段值、对比方式等
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class WhereInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 字段名 */
    private String fieldName;
    /** 字段值，原始值 */
    private Object fieldValue;
    /** 字段值，转换后的值 */
    private String fieldValueStr;
    /** 字段值类型 */
    private Class valueType;
    /** 字段对比方式 */
    private ContrastEnum contrast;
    /** 字段所属表名 */
    private String tableName;
    /** 原始SQL（若存在则直接使用） */
    private String rawSql;
    /** 是否使用安全模式 */
    private Boolean useSafe;
}
