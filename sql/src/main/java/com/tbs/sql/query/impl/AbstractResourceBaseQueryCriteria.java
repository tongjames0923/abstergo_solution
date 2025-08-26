package com.tbs.sql.query.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.sql.framework.model.SqlSelectInfoModel;
import com.tbs.sql.query.IQueryCriteria;

import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象查询对象，查询SQL从Resource中获取
 *
 * @author tongj
 */
public abstract class AbstractResourceBaseQueryCriteria implements IQueryCriteria {

    private String resourcePath;

    private String sql;

    private static Map<String, SqlSelectInfoModel> sqlSelectInfoModelMap = new ConcurrentHashMap<>(32);

    protected SqlSelectInfoModel loadSql(String resourcePath) {
        synchronized (this) {
            try (BufferedReader reader = ResourceUtil.getUtf8Reader(resourcePath)) {
                final StringBuilder sql = new StringBuilder();
                reader.lines().forEach(line -> {
                    sql.append(line);
                });
                SqlSelectInfoModel selectSqlInfo = SqlSelectInfoModel.rawSql(sql.toString().trim());
                return selectSqlInfo;
            } catch (Exception e) {
                AssertUtil.throwError(() -> new FormattedRuntimeException("加载SQL失败：{}", e, resourcePath));
            }
        }
        return null;
    }

    protected void putResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    protected String resourcePath() {
        return resourcePath;
    }

    @Override
    public SqlSelectInfoModel getSelectSQLInfo() {
        return sqlSelectInfoModelMap.computeIfAbsent(resourcePath(), this::loadSql);
    }
}
