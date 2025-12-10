package com.tbs.sql.enums;

/**
 * 查询对比枚举类，定义了查询条件中可用的各种比较操作符
 * <p>
 * 该枚举类包含了常见的SQL查询比较操作符，如等于、不等于、大于、小于等，
 * 以及模糊匹配、空值判断和集合匹配等操作。
 * </p>
 * @author abstergo
 * @since 1.0.0
 */
public enum QueryContrastEnum {

    /**
     * 等于操作符枚举值
     * <p>
     * 对应SQL中的"="操作符
     * 示例：WHERE name = '张三'
     * </p>
     */
    EQUAL,

    /**
     * 不等于操作符枚举值
     * <p>
     * 对应SQL中的"!="或"<>"操作符
     * 示例：WHERE status != 1 或 WHERE status <> 1
     * </p>
     */
    NOT_EQUAL,

    /**
     * 大于操作符枚举值
     * <p>
     * 对应SQL中的">"操作符
     * 示例：WHERE age > 18
     * </p>
     */
    GREATER,

    /**
     * 小于操作符枚举值
     * <p>
     * 对应SQL中的"<"操作符
     * 示例：WHERE price < 100
     * </p>
     */
    LESS,

    /**
     * 大于等于操作符枚举值
     * <p>
     * 对应SQL中的">="操作符
     * 示例：WHERE score >= 60
     * </p>
     */
    GREATER_EQUAL,

    /**
     * 小于等于操作符枚举值
     * <p>
     * 对应SQL中的"<="操作符
     * 示例：WHERE quantity <= 10
     * </p>
     */
    LESS_EQUAL,

    /**
     * 为空判断操作符枚举值
     * <p>
     * 对应SQL中的"IS NULL"操作符
     * 示例：WHERE email IS NULL
     * </p>
     */
    IS_NULL,

    /**
     * 不为空判断操作符枚举值
     * <p>
     * 对应SQL中的"IS NOT NULL"操作符
     * 示例：WHERE phone IS NOT NULL
     * </p>
     */
    IS_NOT_NULL,

    /**
     * 右模糊匹配操作符枚举值，对应SQL中的LIKE '%xxx' 模式
     * <p>
     * 对应SQL中的"LIKE '%xxx'"模式
     * 示例：WHERE name LIKE '%三'
     * </p>
     */
    RLIKE,

    /**
     * 左模糊匹配操作符枚举值，对应SQL中的LIKE 'xxx%' 模式
     * <p>
     * 对应SQL中的"LIKE 'xxx%'"模式
     * 示例：WHERE name LIKE '张%'
     * </p>
     */
    LLIKE,

    /**
     * 不匹配操作符枚举值
     * <p>
     * 对应SQL中的"NOT LIKE"操作符
     * 示例：WHERE name NOT LIKE '%李%'
     * </p>
     */
    NOT_LIKE,

    /**
     * 在集合中操作符枚举值
     * <p>
     * 对应SQL中的"IN"操作符
     * 示例：WHERE status IN (1, 2, 3)
     * </p>
     */
    IN,

    /**
     * 不在集合中操作符枚举值
     * <p>
     * 对应SQL中的"NOT IN"操作符
     * 示例：WHERE status NOT IN (4, 5, 6)
     * </p>
     */
    NOT_IN,

    /**
     * 模糊匹配操作符枚举值，对应SQL中的LIKE '%xx%' 模式
     * <p>
     * 对应SQL中的"LIKE '%xx%'"模式
     * 示例：WHERE name LIKE '%张%'
     * </p>
     */
    LIKE

}
