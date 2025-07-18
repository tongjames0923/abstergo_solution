package com.tbs.sql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlSelectInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 原始sql,若存在则优先使用
     */
    private String rawSql;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 字段列表
     */
    List<SqlSelectItemModel> fieldList;

    public static SqlSelectInfoModel rawSql(String sql) {
        return new SqlSelectInfoModel(sql, null, null);
    }

    public static SqlSelectInfoModel tableName(String tableName) {
        return new SqlSelectInfoModel(null, tableName, null);
    }

    public static SqlSelectInfoModel custom(String tableName, List<SqlSelectItemModel> fieldList) {
        return new SqlSelectInfoModel(null, tableName, fieldList);
    }
}
