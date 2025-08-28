package com.tbs.sql.enums;

/**
 * 查询连接符枚举类，定义了查询条件之间的逻辑连接方式
 * <p>
 * 该枚举类用于指定多个查询条件之间的逻辑关系，包括AND(与)和OR(或)两种连接方式。
 * 在构建复杂查询条件时，通过该枚举来指定条件间的逻辑关系。
 * </p>
 * @author abstergo
 */
public enum QueryConnectorEnum {
    /**
     * OR逻辑连接符枚举值
     * <p>
     * 表示条件之间的"或"逻辑关系，对应SQL中的OR操作符。
     * 示例：WHERE status = 1 OR type = 2
     * </p>
     */
    OR,

    /**
     * AND逻辑连接符枚举值
     * <p>
     * 表示条件之间的"与"逻辑关系，对应SQL中的AND操作符。
     * 示例：WHERE status = 1 AND type = 2
     * </p>
     */
    AND
}
