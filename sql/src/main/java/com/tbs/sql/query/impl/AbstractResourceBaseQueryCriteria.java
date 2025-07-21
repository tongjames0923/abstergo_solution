package com.tbs.sql.query.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.sql.model.SqlSelectInfoModel;
import com.tbs.sql.query.IQueryCriteria;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象查询对象，查询SQL从Resource中获取
 *
 * @author tongj
 */
public abstract class AbstractResourceBaseQueryCriteria implements IQueryCriteria {

    private String resourcePath;

    private String sql;

    private Map<String, SqlSelectInfoModel> sqlSelectInfoModelMap = new HashMap<>(32);

    protected SqlSelectInfoModel loadSql(String resourcePath) {
        try (BufferedReader reader = ResourceUtil.getUtf8Reader(resourcePath)) {
            final StringBuilder sql = new StringBuilder();
            reader.lines().forEach(line -> {
                sql.append(line);
            });
            SqlSelectInfoModel selectSqlInfo = SqlSelectInfoModel.rawSql(sql.toString().trim());
            return selectSqlInfo;
        } catch (Exception e) {
            throw new FormattedRuntimeException("加载SQL失败：{}", e, resourcePath);
        }
    }

    protected void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    protected String getResourcePath() {
        return resourcePath;
    }

    @Override
    public SqlSelectInfoModel getSelectSQLInfo() {
        return sqlSelectInfoModelMap.computeIfAbsent(resourcePath, this::loadSql);
    }
}
