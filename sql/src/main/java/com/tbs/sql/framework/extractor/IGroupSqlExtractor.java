package com.tbs.sql.framework.extractor;

import com.tbs.sql.framework.model.SqlGroupModel;

import java.util.List;

/**
 * 分组SQL提取器
 *
 * @author tbs
 */
public interface IGroupSqlExtractor {
    /**
     * 获取分组SQL
     *
     * @param sqlGroupModels
     * @return
     */
    public String extraGroupBySql(List<SqlGroupModel> sqlGroupModels);
}
