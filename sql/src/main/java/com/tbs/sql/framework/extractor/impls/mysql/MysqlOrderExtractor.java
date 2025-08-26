package com.tbs.sql.framework.extractor.impls.mysql;

import cn.hutool.core.collection.CollUtil;
import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.framework.extractor.IOrderSqlExtractor;
import com.tbs.sql.framework.model.SqlOrderInfo;

import java.util.List;

/**
 * mysql 排序sql提取器
 *
 * @author abstergo
 */
public class MysqlOrderExtractor implements IOrderSqlExtractor {
    @Override
    public String extraOrderSql(List<SqlOrderInfo> sqlOrderInfos) {
        if (CollUtil.isEmpty(sqlOrderInfos)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (SqlOrderInfo info : sqlOrderInfos) {
            sb.append(info.getFiledName()).append(" ")
                .append(QueryOrderEnum.getOrder(info.getOrder() == null ? QueryOrderEnum.ASC : info.getOrder()))
                .append(",");
        }
        return " ORDER BY " + sb.substring(0, sb.length() - 1);
    }
}
