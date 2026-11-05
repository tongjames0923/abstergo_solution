package com.tbs.sql.assembler.impl;

import com.tbs.sql.assembler.ISqlAssembler;

import java.util.StringJoiner;

/**
 * 默认SQL组装器实现类，将各个SQL片段按照标准SQL语法组装成完整的查询语句。
 *
 * <p>该类实现了 {@link ISqlAssembler} 接口，提供了标准的SQL组装逻辑。它将SELECT、WHERE、
 * GROUP BY、ORDER BY、LIMIT等SQL片段按照SQL语法顺序组装成完整的查询语句。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>作为默认的SQL组装器使用</li>
 *   <li>需要标准SQL语法组装时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * ISqlAssembler assembler = new DefaultSqlAssembler();
 * String sql = assembler.assembleSelectSql(
 *     "SELECT u.*, d.name as dept_name",
 *     "FROM users u LEFT JOIN departments d ON u.dept_id = d.id",
 *     "WHERE u.status = 1",
 *     "ORDER BY u.create_time",
 *     null,
 *     "LIMIT 10, 20"
 * );
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该实现使用空格连接各个SQL片段</li>
 *   <li>空值或空字符串会被自动忽略</li>
 *   <li>如果需要自定义组装逻辑，可以实现 {@link ISqlAssembler} 接口</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see ISqlAssembler
 */
public class DefaultSqlAssembler implements ISqlAssembler {
    private static final String SPACE = " ";

    /**
     * 将各个SQL片段组装成完整的SELECT语句。
     *
     * @param selectSql SELECT子句，如 "SELECT * FROM users"
     * @param whereSql WHERE子句，如 "WHERE status = 1"，可以为null
     * @param orderSql ORDER BY子句，如 "ORDER BY create_time DESC"，可以为null
     * @param groupSql GROUP BY子句，如 "GROUP BY status"，可以为null
     * @param limitSql LIMIT子句，如 "LIMIT 10"，可以为null
     * @return 完整的SQL语句
     */
    @Override
    public String assembleSelectSql(String selectSql, String whereSql, String orderSql, String groupSql, String limitSql) {
        StringJoiner joiner = new StringJoiner(SPACE);
        if (selectSql != null && !selectSql.isEmpty()) joiner.add(selectSql);
        if (whereSql != null && !whereSql.isEmpty()) joiner.add(whereSql);
        if (groupSql != null && !groupSql.isEmpty()) joiner.add(groupSql);
        if (orderSql != null && !orderSql.isEmpty()) joiner.add(orderSql);
        if (limitSql != null && !limitSql.isEmpty()) joiner.add(limitSql);
        return joiner.toString();
    }
}
