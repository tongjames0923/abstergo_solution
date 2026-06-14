package com.tbs.sql.mapper.impl;

import java.util.Map;

/**
 * 原始SQL查询提供器类，提供动态SQL生成方法。
 *
 * <p>该类提供了动态SQL生成方法，用于MyBatis的 {@link org.apache.ibatis.annotations.SelectProvider} 注解。
 * 支持直接SQL查询和统计查询两种方式。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>提供动态SQL查询方法 {@link #dynamicSql}</li>
 *   <li>提供动态统计查询方法 {@link #dynamicCountSql}</li>
 *   <li>支持参数化SQL</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在 {@link com.tbs.sql.mapper.IRawSqlSelectMapper} 中提供动态SQL</li>
 *   <li>执行自定义SQL查询</li>
 *   <li>执行SQL统计</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在Mapper中使用
 * @SelectProvider(type = RawSqlSelectProvider.class, method = "dynamicSql")
 * List<Map<String, Object>> fetch(String sql, Map<String, Object> params);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>SQL语句直接返回，不做任何处理</li>
 *   <li>统计查询会自动包装为count查询</li>
 *   <li>参数需要与SQL语句中的占位符匹配</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IRawSqlSelectMapper
 */
public class RawSqlSelectProvider {
    public String dynamicSql(String sql, Map<String, Object> _prs) { return sql; }
    public String dynamicCountSql(String sql, Map<String, Object> _prs) { return "select count(*) from (" + sql + ") as t1"; }
}
