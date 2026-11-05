package com.tbs.sql.assembler;

/**
 * SQL组装器接口，用于将各个SQL片段组装成完整的SQL语句。
 *
 * <p>该接口定义了SQL组装的标准方法，将SELECT、WHERE、ORDER BY、GROUP BY、LIMIT等
 * SQL片段按照标准SQL语法组装成完整的查询语句。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>将各个SQL片段组装成完整的查询语句</li>
 *   <li>支持不同数据库的SQL语法差异</li>
 *   <li>可自定义SQL组装逻辑</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用默认组装器
 * ISqlAssembler assembler = new DefaultSqlAssembler();
 * String sql = assembler.assembleSelectSql(
 *     "SELECT * FROM users",
 *     "WHERE status = 1",
 *     "ORDER BY create_time",
 *     null,
 *     "LIMIT 10"
 * );
 * // 结果: SELECT * FROM users WHERE status = 1 ORDER BY create_time LIMIT 10
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实现类需要确保生成的SQL语法正确</li>
 *   <li>可以自定义组装逻辑以支持不同的SQL方言</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see DefaultSqlAssembler
 */
public interface ISqlAssembler {
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
    String assembleSelectSql(String selectSql, String whereSql, String orderSql, String groupSql, String limitSql);
}
