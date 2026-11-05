package com.tbs.sql.dsl.clause;

import com.tbs.common.function.SFunction;

/**
 * 关联条件类，用于定义子查询与主表之间的关联关系。
 *
 * <p>该类封装了子查询字段与主表字段的关联条件，支持多个关联条件的组合。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 单个关联条件
 * JoinCondition.of(UserDepartmentRelation::getDepartmentId, Department::getId)
 *
 * // 多个关联条件
 * JoinCondition.of(OrderItem::getOrderId, Order::getId),
 * JoinCondition.of(OrderItem::getProductId, Product::getId)
 * }</pre>
 *
 * @param <SE> 子查询实体类型
 * * @param <ME> 主表实体类型
 * @param <P>  字段类型
 * @author abstergo
 * @since 2.0.0
 */
public class JoinCondition<SE, ME, P> {
    private final SFunction<SE, P> subField;
    private final SFunction<ME, P> mainField;

    private JoinCondition(SFunction<SE, P> subField, SFunction<ME, P> mainField) {
        this.subField = subField;
        this.mainField = mainField;
    }

    /**
     * 创建关联条件。
     *
     * @param subField  子查询表的关联字段
     * @param mainField 主表的关联字段
     * @param <SE>      子查询实体类型
     * @param <ME>      主表实体类型
     * @param <P>       字段类型
     * @return 关联条件实例
     */
    public static <SE, ME, P> JoinCondition<SE, ME, P> of(SFunction<SE, P> subField, SFunction<ME, P> mainField) {
        return new JoinCondition<>(subField, mainField);
    }

    public SFunction<SE, P> getSubField() {
        return subField;
    }

    public SFunction<ME, P> getMainField() {
        return mainField;
    }
}
