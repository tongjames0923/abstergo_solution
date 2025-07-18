package com.tbs.sql.extractor;

import com.tbs.sql.model.SqlSelectInfoModel;

/**
 * select 语句提取器
 *
 * @author abstergo
 */
public interface ISelectSqlExtractor {
    /**
     * 提取select语句
     *
     * @param infoModel
     * @return
     */
    String extractSelectSql(SqlSelectInfoModel infoModel);
}
