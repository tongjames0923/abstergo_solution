package com.tbs.sql.constants;

/**
 * 排序常量定义类，包含排序相关的Bean名称常量。
 *
 * <p>该类定义了排序策略Bean的名称常量，用于在Spring容器中查找和注册排序策略Bean。
 * 这些常量在 {@link com.tbs.sql.annotation.order.QueryOrderField} 注解中使用。</p>
 *
 * <h3>常量说明</h3>
 * <ul>
 *   <li>{@link #ASC}: 升序排序策略Bean名称</li>
 *   <li>{@link #DESC}: 降序排序策略Bean名称</li>
 *   <li>{@link #ORDERED_MODEL}: 有序模型排序策略Bean名称</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在 {@link com.tbs.sql.annotation.order.QueryOrderField} 注解中指定排序策略</li>
 *   <li>在Spring容器中查找排序策略Bean</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在注解中使用
 * @QueryOrderField(mappedName = "create_time", order = OrderConstants.ASC)
 * private Boolean orderByCreateTime;
 * }</pre>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.annotation.order.QueryOrderField
 * @see com.tbs.sql.order.IQueryOrderBy
 */
public final class OrderConstants {
    public static final String ASC = "ASC_ORDER_BEAN";
    public static final String DESC = "DESC_ORDER_BEAN";
    public static final String ORDERED_MODEL = "ORDERED_MODEL";
}
