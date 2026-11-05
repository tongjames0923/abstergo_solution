package com.tbs.sql.order.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.IQueryOrderBy;
import com.tbs.sql.order.IQueryOrderedModel;

import java.lang.reflect.Field;

/**
 * 有序模型排序策略实现类，从 {@link IQueryOrderedModel} 获取排序方向。
 *
 * <p>该类实现了 {@link IQueryOrderBy} 接口，通过反射从 {@link IQueryOrderedModel}
 * 实现类中获取排序方向。支持动态排序决策。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从 {@link IQueryOrderedModel} 获取排序方向</li>
 *   <li>支持动态排序决策</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在 {@link com.tbs.sql.annotation.order.QueryOrderField} 注解中指定动态排序</li>
 *   <li>需要根据业务逻辑动态决定排序方向时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在注解中使用
 * @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
 * private OrderedModel orderByCreateTime;
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>字段类型必须实现 {@link IQueryOrderedModel} 接口</li>
 *   <li>字段值为null时返回null</li>
 *   <li>通过反射访问字段，可能有性能开销</li>
 * </ul>
 *
 * @author abstergo
 * @see IQueryOrderBy
 * @see IQueryOrderedModel
 * @see OrderedModel
 * @since 1.0.0
 */
public class OrderedModelOrderImpl implements IQueryOrderBy {
    @Override
    public OrderEnum orderBy(Object target, Field field, String mappedName) {
        OrderEnum result = null;
        if (IQueryOrderedModel.class.isAssignableFrom(field.getType())) {
            // 共享Field的可访问状态在多线程下反复切换会引发竞态，此处只保证可访问，不再重置
            field.setAccessible(true);
            try {
                IQueryOrderedModel orderedModel = (IQueryOrderedModel) field.get(target);
                if (orderedModel == null) {
                    return null;
                }
                result = orderedModel.getOrdered();
            } catch (Exception e) {
                AssertUtil.throwError(() -> new FormattedRuntimeException(e, "获取OrderedModel失败"));
            }
        } else {
            AssertUtil.throwError(
                () -> new FormattedRuntimeException("OrderedModelOrderImpl只支持IQueryOrderedModel").errorLevel(
                    ErrorLevelEnum.CRITICAL_SYSTEM_ERROR));
        }
        return result;
    }
}
