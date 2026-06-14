package com.tbs.sql.order;

import com.tbs.sql.enums.OrderEnum;

/**
 * 有序模型接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IQueryOrderedModel {
    OrderEnum getOrdered();
}
