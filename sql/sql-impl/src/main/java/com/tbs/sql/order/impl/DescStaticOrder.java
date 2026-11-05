package com.tbs.sql.order.impl;

import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.IQueryOrderBy;

import java.lang.reflect.Field;

/**
 * 降序排序策略实现类，始终返回降序排序方向。
 *
 * <p>该类实现了 {@link IQueryOrderBy} 接口，始终返回 {@link OrderEnum#DESC} 降序排序方向。
 * 用于在 {@link com.tbs.sql.annotation.order.QueryOrderField} 注解中指定降序排序。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在 {@link com.tbs.sql.annotation.order.QueryOrderField} 注解中指定降序排序</li>
 *   <li>需要固定降序排序时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在注解中使用
 * @QueryOrderField(mappedName = "create_time", order = OrderConstants.DESC)
 * private Boolean orderByCreateTime;
 * }</pre>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IQueryOrderBy
 * @see AscStaticOrder
 */
public final class DescStaticOrder implements IQueryOrderBy {
    @Override
    public OrderEnum orderBy(Object target, Field field, String mappedName) {
        return OrderEnum.DESC;
    }
}
