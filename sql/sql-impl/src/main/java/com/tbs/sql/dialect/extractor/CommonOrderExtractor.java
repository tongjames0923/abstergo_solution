package com.tbs.sql.dialect.extractor;

import cn.hutool.core.collection.CollUtil;
import com.tbs.sql.dialect.IOrderExtractor;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.model.order.OrderInfo;

import java.util.List;

/**
 * 通用ORDER BY语句提取器，适用于MySQL、PostgreSQL、SQL Server、Oracle等数据库。
 *
 * <p>所有主流关系型数据库的ORDER BY语法（ASC/DESC、多字段排序）完全一致，
 * 因此使用单一公共实现，避免各方言重复定义。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IOrderExtractor
 */
public class CommonOrderExtractor implements IOrderExtractor {

    @Override
    public String extract(List<OrderInfo> orderInfos) {
        if (CollUtil.isEmpty(orderInfos)) return "";
        StringBuilder sb = new StringBuilder();
        for (OrderInfo info : orderInfos) {
            sb.append(info.getFieldName()).append(" ")
                .append(OrderEnum.getOrder(info.getOrder() == null ? OrderEnum.ASC : info.getOrder()))
                .append(",");
        }
        return " ORDER BY " + sb.substring(0, sb.length() - 1);
    }
}
