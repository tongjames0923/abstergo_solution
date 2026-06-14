package com.tbs.sql.enums;

/**
 * 查询排序枚举类，定义了查询结果的排序方式
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum OrderEnum {
    DESC,
    ASC;

    /**
     * 根据字符串获取对应的排序枚举值
     *
     * @param order 排序方式字符串
     * @return 对应的OrderEnum枚举值
     */
    public static OrderEnum getOrder(String order) {
        if (order.equalsIgnoreCase("desc")) {
            return DESC;
        } else {
            return ASC;
        }
    }

    /**
     * 根据排序枚举值获取对应的字符串表示
     *
     * @param order OrderEnum枚举值
     * @return 对应的字符串表示
     */
    public static String getOrder(OrderEnum order) {
        if (order.equals(DESC)) {
            return "desc";
        } else {
            return "asc";
        }
    }
}
