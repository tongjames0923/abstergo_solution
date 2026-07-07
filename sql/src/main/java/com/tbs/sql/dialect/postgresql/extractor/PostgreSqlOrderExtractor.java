package com.tbs.sql.dialect.postgresql.extractor;

import cn.hutool.core.collection.CollUtil;
import com.tbs.sql.dialect.IOrderExtractor;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.model.order.OrderInfo;

import java.util.List;

/**
 * PostgreSQL ORDER BY语句提取器。
 *
 * <p>该类实现了 {@link IOrderExtractor} 接口，提供了PostgreSQL特有的ORDER BY语句提取功能。
 * PostgreSQL的ORDER BY语法与MySQL/SQL Server一致，支持多字段排序、升序和降序。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IOrderExtractor
 * @see OrderInfo
 */
public class PostgreSqlOrderExtractor implements IOrderExtractor {

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
