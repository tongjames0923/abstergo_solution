package com.tbs.sql.order.impl;

import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.IQueryOrderedModel;

import java.io.Serializable;

/**
 * 动态排序模型的实现类，支持运行时修改排序
 *
 * @author abstergo
 * @since 1.0.0
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
