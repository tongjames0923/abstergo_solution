package com.tbs.sql.dialect;

import com.tbs.sql.model.limit.LimitModel;

/**
 * LIMIT语句提取器
 */
public interface ILimitExtractor {
    String extract(LimitModel model);
}
