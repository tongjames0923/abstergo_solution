package com.tbs.sql.dialect;

import com.tbs.sql.model.order.OrderInfo;

import java.util.List;

/**
 * ORDER BY语句提取器
 */
public interface IOrderExtractor {
    String extract(List<OrderInfo> orderInfos);
}
