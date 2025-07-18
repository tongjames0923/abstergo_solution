package com.tbs.sql.extractor;

import com.tbs.sql.model.SqlOrderInfo;

import java.util.List;

/**
 * 排序SQL提取器
 *
 * @author tbs
 */
public interface IOrderSqlExtractor {

    /**
     * 获取排序SQL
     *
     * @param sqlOrderInfos
     * @return
     */
    public String extraOrderSql(List<SqlOrderInfo> sqlOrderInfos);

}
