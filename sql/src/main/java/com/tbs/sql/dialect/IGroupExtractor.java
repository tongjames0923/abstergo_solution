package com.tbs.sql.dialect;

import com.tbs.sql.model.group.GroupModel;

import java.util.List;

/**
 * GROUP BY语句提取器
 */
public interface IGroupExtractor {
    String extract(List<GroupModel> groupModels);
}
