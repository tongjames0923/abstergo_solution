package com.tbs.sql.criteria;

import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.sql.model.select.SelectInfoModel;

import cn.hutool.core.io.resource.ResourceUtil;

/**
 * 抽象查询对象，查询SQL从Resource中获取
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class AbstractResourceCriteria implements IQueryCriteria {

    private String resourcePath;
    private static Map<String, SelectInfoModel> sqlSelectInfoModelMap = new ConcurrentHashMap<>(32);

    protected SelectInfoModel loadSql(String resourcePath) {
        try (BufferedReader reader = ResourceUtil.getUtf8Reader(resourcePath)) {
            final StringBuilder sql = new StringBuilder();
            reader.lines().forEach(sql::append);
            return SelectInfoModel.rawSql(sql.toString().trim());
        } catch (Exception e) {
            AssertUtil.throwError(() -> new FormattedRuntimeException("加载SQL失败：{}", e, resourcePath));
        }
        throw new FormattedRuntimeException("加载SQL失败：{}", resourcePath);
    }

    protected void putResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    protected String resourcePath() {
        return resourcePath;
    }

    @Override
    public SelectInfoModel getSelectSQLInfo() {
        return sqlSelectInfoModelMap.computeIfAbsent(resourcePath(), this::loadSql);
    }
}
