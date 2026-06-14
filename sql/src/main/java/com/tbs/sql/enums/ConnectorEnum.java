package com.tbs.sql.enums;

/**
 * 查询连接符枚举类，定义了查询条件之间的逻辑连接方式
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum ConnectorEnum {
    /**
     * OR逻辑连接符枚举值
     * <p>对应SQL中的OR操作符</p>
     */
    OR,

    /**
     * AND逻辑连接符枚举值
     * <p>对应SQL中的AND操作符</p>
     */
    AND
}
