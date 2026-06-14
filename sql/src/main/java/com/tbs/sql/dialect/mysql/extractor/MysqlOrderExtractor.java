package com.tbs.sql.dialect.mysql.extractor;

import cn.hutool.core.collection.CollUtil;
import com.tbs.sql.dialect.IOrderExtractor;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.model.order.OrderInfo;

import java.util.List;

public class MysqlOrderExtractor implements IOrderExtractor {
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
