package com.tbs.sql.query;

import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.sql.framework.model.SqlSelectInfoModel;

import cn.hutool.core.io.resource.ResourceUtil;

/**
 * 抽象查询对象，查询SQL从Resource中获取
 *
 * @author tongj
 */
public abstract class AbstractResourceBaseQueryCriteria implements IQueryCriteria {

	private String resourcePath;

	private String sql;

	private static Map<String, SqlSelectInfoModel> sqlSelectInfoModelMap = new ConcurrentHashMap<>(32);

	/**
	 * 从指定资源路径加载SQL语句
	 *
	 * @param resourcePath 资源路径
	 * @return SqlSelectInfoModel SQL信息模型
	 */
	protected SqlSelectInfoModel loadSql(String resourcePath) {
		try (BufferedReader reader = ResourceUtil.getUtf8Reader(resourcePath)) {
			final StringBuilder sql = new StringBuilder();
			reader.lines().forEach(sql::append);
			return SqlSelectInfoModel.rawSql(sql.toString().trim());
		} catch (Exception e) {
			AssertUtil.throwError(() -> new FormattedRuntimeException("加载SQL失败：{}", e, resourcePath));
		}
		// unreachable: throwError always throws
		throw new FormattedRuntimeException("加载SQL失败：{}", resourcePath);
	}

	/**
	 * 设置资源路径
	 *
	 * @param resourcePath 资源路径
	 */
	protected void putResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/**
	 * 获取资源路径
	 *
	 * @return 资源路径
	 */
	protected String resourcePath() {
		return resourcePath;
	}

	/**
	 * 获取选择SQL信息
	 *
	 * @return SqlSelectInfoModel SQL信息模型
	 */
	@Override
	public SqlSelectInfoModel getSelectSQLInfo() {
		return sqlSelectInfoModelMap.computeIfAbsent(resourcePath(), this::loadSql);
	}
}
