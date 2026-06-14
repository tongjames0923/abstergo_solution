package com.tbs.sql.enums;

/**
 * 查询对比枚举类，定义了查询条件中可用的各种比较操作符
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum ContrastEnum {
    EQUAL,
    NOT_EQUAL,
    GREATER,
    LESS,
    GREATER_EQUAL,
    LESS_EQUAL,
    IS_NULL,
    IS_NOT_NULL,
    RLIKE,
    LLIKE,
    NOT_LIKE,
    IN,
    NOT_IN,
    LIKE
}
