package com.tbs.sql.order.impl;

import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.IQueryOrderedModel;

import java.io.Serializable;

/**
 * 动态排序模型实现类，支持运行时修改排序方向。
 *
 * <p>该类实现了 {@link IQueryOrderedModel} 接口，提供了动态设置排序方向的功能。
 * 可以在运行时根据业务需求修改排序方向。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>支持动态设置排序方向</li>
 *   <li>支持序列化</li>
 *   <li>提供默认构造函数和带参构造函数</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要动态修改排序方向时</li>
 *   <li>在查询对象中使用动态排序</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建动态排序模型
 * OrderedModel model = new OrderedModel(OrderEnum.DESC);
 * 
 * // 修改排序方向
 * model.setOrdered(OrderEnum.ASC);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>排序方向可以为null，表示不应用排序</li>
 *   <li>支持序列化，可以用于网络传输</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IQueryOrderedModel
 * @see OrderedModelOrderImpl
 */
public class OrderedModel implements IQueryOrderedModel, Serializable {
    private static final long serialVersionUID = -1L;

    private OrderEnum ordered;

    public OrderedModel(OrderEnum ordered) {
        this.ordered = ordered;
    }

    public OrderedModel() {
    }

    public void setOrdered(OrderEnum ordered) {
        this.ordered = ordered;
    }

    @Override
    public OrderEnum getOrdered() {
        return ordered;
    }
}
