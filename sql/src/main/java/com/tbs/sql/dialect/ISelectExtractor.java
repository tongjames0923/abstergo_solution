package com.tbs.sql.dialect;

import com.tbs.sql.model.select.SelectInfoModel;

/**
 * SELECT语句提取器
 */
public interface ISelectExtractor {
    String extract(SelectInfoModel infoModel);
}
