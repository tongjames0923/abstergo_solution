package com.tbs.sql.enums;

/**
 * 查询排序枚举类，定义了查询结果的排序方式
 * <p>
 * 该枚举类包含两种排序方式：降序(DESC)和升序(ASC)，
 * 并提供了在字符串表示和枚举值之间相互转换的方法。
 * </p>
 * @author abstergo
 * @since 1.0.0
 */
public enum QueryOrderEnum {
    /**
     * 降序排序枚举值
     * <p>对应SQL中的DESC关键字</p>
     * 示例：ORDER BY create_time DESC
     */
    DESC,
    /**
     * 升序排序枚举值
     * <p>对应SQL中的ASC关键字</p>
     * 示例：ORDER BY create_time ASC
     */
    ASC;

    /**
     * 根据字符串获取对应的排序枚举值
     * <p>
     * 该方法将输入的字符串转换为相应的排序枚举值。 不区分大小写，当输入"desc"(忽略大小写)时返回DESC枚举值，其他情况返回ASC枚举值。
     * </p>
     *
     * @param order 排序方式字符串，如"desc"或"asc"
     * @return 对应的QueryOrderEnum枚举值，如果输入为"desc"(忽略大小写)则返回DESC，否则返回ASC
     */
    public static QueryOrderEnum getOrder(String order) {
        if (order.equalsIgnoreCase("desc")) {
            return DESC;
        } else {
            return ASC;
        }
    }

    /**
     * 根据排序枚举值获取对应的字符串表示
     * <p>
     * 该方法将排序枚举值转换为相应的字符串表示形式。 当输入为DESC时返回"desc"，其他情况返回"asc"。
     * </p>
     *
     * @param order QueryOrderEnum枚举值
     * @return 对应的字符串表示，DESC返回"desc"，ASC返回"asc"
     */
    public static String getOrder(QueryOrderEnum order) {
        if (order.equals(DESC)) {
            return "desc";
        } else {
            return "asc";
        }
    }
}
