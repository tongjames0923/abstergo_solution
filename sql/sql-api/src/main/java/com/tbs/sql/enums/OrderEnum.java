package com.tbs.sql.enums;

/**
 * 查询排序枚举类，定义了查询结果的排序方式。
 *
 * <p>该枚举类定义了SQL查询中使用的排序方式，包括升序和降序两种。
 * 提供了字符串与枚举值之间的转换方法。</p>
 *
 * <h3>枚举值说明</h3>
 * <ul>
 *   <li>{@link #ASC}: 升序排序，对应SQL中的ASC关键字</li>
 *   <li>{@link #DESC}: 降序排序，对应SQL中的DESC关键字</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在DSL查询中指定排序方式</li>
 *   <li>在 {@link com.tbs.sql.model.order.OrderInfo} 中设置排序方向</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在DSL中使用
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .getOrderSupport()
 *     .ifPresent(order -> {
 *         order.orderByDesc(User::getCreateTime);
 *     })
 *     .fetch();
 * 
 * // 字符串转换
 * OrderEnum order = OrderEnum.getOrder("desc");  // 返回DESC
 * String orderStr = OrderEnum.getOrder(OrderEnum.ASC);  // 返回"asc"
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>默认使用升序排序</li>
 *   <li>多个字段排序时，按声明顺序应用</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.model.order.OrderInfo
 * @see com.tbs.sql.dsl.IOrderClause
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
