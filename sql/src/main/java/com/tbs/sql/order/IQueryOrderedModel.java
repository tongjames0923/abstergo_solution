package com.tbs.sql.order;

import com.tbs.sql.enums.OrderEnum;

/**
 * 有序模型接口，定义了获取排序方向的方法。
 *
 * <p>该接口定义了获取排序方向的方法，允许模型对象实现排序功能。
 * 通常与 {@link com.tbs.sql.order.impl.OrderedModelOrderImpl} 配合使用。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取排序方向 {@link #getOrdered()}</li>
 *   <li>支持动态排序</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>实现动态排序功能</li>
 *   <li>在运行时修改排序方向</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 实现有序模型接口
 * public class UserQuery implements IQueryOrderedModel {
 *     private OrderEnum order = OrderEnum.DESC;
 *
 *     @Override
 *     public OrderEnum getOrdered() {
 *         return order;
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>返回null表示不应用排序</li>
 *   <li>通常与 {@link com.tbs.sql.order.impl.OrderedModelOrderImpl} 配合使用</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.order.impl.OrderedModel
 * @see com.tbs.sql.order.impl.OrderedModelOrderImpl
 */
public interface IQueryOrderedModel {
    /**
     * 获取排序方向。
     *
     * @return 排序方向枚举值，返回null表示不应用排序
     */
    OrderEnum getOrdered();
}
