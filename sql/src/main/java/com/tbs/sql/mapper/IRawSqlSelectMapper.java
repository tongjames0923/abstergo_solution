package com.tbs.sql.mapper;

import com.tbs.sql.mapper.impl.RawSqlSelectProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

/**
 * 原始SQL查询Mapper接口，提供直接执行SQL语句的能力。
 *
 * <p>该接口提供了直接执行SQL语句的方法，支持查询和统计操作。
 * 通过 {@link RawSqlSelectProvider} 提供动态SQL支持。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>执行SQL查询并返回结果列表 {@link #fetch(String, Map)}</li>
 *   <li>执行SQL统计并返回总数 {@link #count(String, Map)}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要执行复杂SQL查询时</li>
 *   <li>需要执行自定义SQL语句时</li>
 *   <li>框架内置查询无法满足需求时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 执行SQL查询
 * String sql = "SELECT * FROM users WHERE status = :status";
 * Map<String, Object> params = Map.of("status", 1);
 * List<Map<String, Object>> results = mapper.fetch(sql, params);
 *
 * // 执行SQL统计
 * Long count = mapper.count(sql, params);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>SQL语句中的参数使用命名参数格式</li>
 *   <li>注意SQL注入风险，避免直接拼接用户输入</li>
 *   <li>返回结果为Map格式，需要手动转换</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see RawSqlSelectProvider
 */
@Mapper
public interface IRawSqlSelectMapper {
    @SelectProvider(type = RawSqlSelectProvider.class, method = "dynamicSql")
    List<Map<String, Object>> fetch(@Param("sql") String sql, @Param("_prs") Map<String, Object> _prs);

    @SelectProvider(type = RawSqlSelectProvider.class, method = "dynamicCountSql")
    Integer count(@Param("sql") String sql, @Param("_prs") Map<String, Object> _prs);
}
