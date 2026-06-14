package com.tbs.sql.order;

import com.tbs.sql.enums.OrderEnum;

import java.lang.reflect.Field;

/**
 * 排序策略接口，定义了排序策略的标准方法。
 *
 * <p>该接口定义了获取排序方向的方法，允许根据实体对象、字段和映射名称
 * 动态决定排序方向。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取排序方向 {@link #orderBy(Object, Field, String)}</li>
 *   <li>支持动态排序决策</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在 {@link com.tbs.sql.annotation.order.QueryOrderField} 注解中指定排序策略</li>
 *   <li>实现自定义排序逻辑</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 实现排序策略
 * public class CustomOrder implements IQueryOrderBy {
 *     @Override
 *     public OrderEnum orderBy(Object target, Field field, String mappedName) {
 *         // 根据条件返回排序方向
 *         return OrderEnum.DESC;
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实现类需要注册为Spring Bean</li>
 *   <li>返回null表示不应用排序</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see OrderEnum
 * @see com.tbs.sql.annotation.order.QueryOrderField
 */
public interface IQueryOrderBy {
    /**
     * 获取排序方向。
     *
     * @param target     实体对象
     * @param field      字段对象
     * @param mappedName 字段映射名称
     * @return 排序方向，返回null表示不应用排序
     */
    OrderEnum orderBy(Object target, Field field, String mappedName);
}
