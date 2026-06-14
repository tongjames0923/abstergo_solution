package com.tbs.sql.model.select;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SQL查询信息模型
 * <p>
 * 封装SQL查询的各种信息，包括原始SQL、表名、字段列表和实体类
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 原始SQL语句 */
    private String rawSql;
    /** 表名 */
    private String tableName;
    /** 字段列表 */
    List<SelectItemModel> fieldList;
    /** 实体类 */
    private Class<?> entityClass;

    public static SelectInfoModel rawSql(String sql) {
        return new SelectInfoModel(sql, null, null, null);
    }

    public static SelectInfoModel tableName(String tableName) {
        return new SelectInfoModel(null, tableName, null, null);
    }

    public static SelectInfoModel custom(String tableName, List<SelectItemModel> fieldList) {
        return new SelectInfoModel(null, tableName, fieldList, null);
    }
}
