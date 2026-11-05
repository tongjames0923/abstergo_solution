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
    /** 字段所属表别名 */
    private String tableAlias;
    /** 原始SQL（若存在则直接使用） */
    private String rawSql;

    /**
     * 创建Builder实例
     *
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * WHERE条件构建器，支持链式调用
     */
    public static class Builder {
        private final WhereInfo info = new WhereInfo();

        private Builder() {
        }

        /**
         * 设置字段名
         *
         * @param fieldName 字段名
         * @return 当前Builder实例
         */
        public Builder fieldName(String fieldName) {
            info.fieldName = fieldName;
            return this;
        }

        /**
         * 设置字段值
         *
         * @param fieldValue 字段值
         * @return 当前Builder实例
         */
        public Builder fieldValue(Object fieldValue) {
            info.fieldValue = fieldValue;
            return this;
        }

        /**
         * 设置字段值（转换后的字符串值）
         *
         * @param fieldValueStr 转换后的字符串值
         * @return 当前Builder实例
         */
        public Builder fieldValueStr(String fieldValueStr) {
            info.fieldValueStr = fieldValueStr;
            return this;
        }

        /**
         * 设置字段值类型
         *
         * @param valueType 字段值类型
         * @return 当前Builder实例
         */
        public Builder valueType(Class valueType) {
            info.valueType = valueType;
            return this;
        }

        /**
         * 设置字段对比方式
         *
         * @param contrast 对比方式枚举
         * @return 当前Builder实例
         */
        public Builder contrast(ContrastEnum contrast) {
            info.contrast = contrast;
            return this;
        }

        /**
         * 设置字段所属表名
         *
         * @param tableName 表名
         * @return 当前Builder实例
         */
        public Builder tableName(String tableName) {
            info.tableName = tableName;
            return this;
        }

        /**
         * 设置字段所属表别名
         *
         * @param tableAlias 表别名
         * @return 当前Builder实例
         */
        public Builder tableAlias(String tableAlias) {
            info.tableAlias = tableAlias;
            return this;
        }

        /**
         * 设置原始SQL
         * <p>
         * 当rawSql不为null时，将直接使用该SQL作为条件，忽略其他字段配置
         * </p>
         *
         * @param rawSql 原始SQL
         * @return 当前Builder实例
         */
        public Builder rawSql(String rawSql) {
            info.rawSql = rawSql;
            return this;
        }

        /**
         * 构建WhereInfo实例
         *
         * @return WhereInfo实例
         */
        public WhereInfo build() {
            return info;
        }
    }
}
