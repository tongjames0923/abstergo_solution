package com.tbs.sql.extractor;

import com.tbs.sql.model.SqlLimitModel;

/**
 * 分页SQL提取器
 *
 * @author tbs
 */
public interface ILimitSqlExtractor {
    /**
     * 获取分页SQL
     *
     * @return
     */
    public String extraLimitSql(SqlLimitModel model);

}
