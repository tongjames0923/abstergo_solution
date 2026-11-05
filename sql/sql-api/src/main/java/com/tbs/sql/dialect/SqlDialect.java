package com.tbs.sql.dialect;

/**
 * SQL方言接口，定义了不同数据库方言的标准方法。
 *
 * <p>该接口是数据库方言的核心接口，定义了获取各种SQL片段提取器的方法。
 * 不同的数据库实现此接口以提供各自的SQL语法支持。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取SELECT提取器 {@link #getSelectExtractor()}</li>
 *   <li>获取WHERE提取器 {@link #getWhereExtractor()}</li>
 *   <li>获取ORDER BY提取器 {@link #getOrderExtractor()}</li>
 *   <li>获取GROUP BY提取器 {@link #getGroupExtractor()}</li>
 *   <li>获取LIMIT提取器 {@link #getLimitExtractor()}</li>
 *   <li>获取INSERT提取器 {@link #getInsertExtractor()}</li>
 *   <li>获取方言名称 {@link #name()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>为不同数据库提供方言支持</li>
 *   <li>在SQL生成过程中使用对应的提取器</li>
 *   <li>扩展支持新的数据库类型</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 获取MySQL方言
 * SqlDialect dialect = new MysqlDialect(...);
 * 
 * // 使用方言提取SQL片段
 * String selectSql = dialect.getSelectExtractor().extract(selectInfo);
 * String whereSql = dialect.getWhereExtractor().extract(whereCompose, params);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>每个数据库需要实现自己的方言类</li>
 *   <li>提取器需要正确处理数据库特定的语法差异</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.dialect.mysql.MysqlDialect
 */
public interface SqlDialect {
    /**
     * 获取GROUP BY语句提取器。
     *
     * @return GROUP BY语句提取器实例
     */
    IGroupExtractor getGroupExtractor();

    /**
     * 获取ORDER BY语句提取器。
     *
     * @return ORDER BY语句提取器实例
     */
    IOrderExtractor getOrderExtractor();

    /**
     * 获取LIMIT语句提取器。
     *
     * @return LIMIT语句提取器实例
     */
    ILimitExtractor getLimitExtractor();

    /**
     * 获取SELECT语句提取器。
     *
     * @return SELECT语句提取器实例
     */
    ISelectExtractor getSelectExtractor();

    /**
     * 获取WHERE语句提取器。
     *
     * @return WHERE语句提取器实例
     */
    IWhereExtractor getWhereExtractor();

    /**
     * 获取INSERT语句提取器。
     *
     * <p>各方言实现按需返回标准多行 {@code VALUES} 或专用语法（如 Oracle 的 {@code INSERT ALL}）
     * 的提取器，接口不预设具体实现，避免契约层耦合具体后端。</p>
     *
     * @return INSERT语句提取器实例
     */
    IInsertExtractor getInsertExtractor();

    /**
     * 获取UPDATE语句提取器。
     *
     * <p>单表 UPDATE 四库语法一致，各方言默认返回通用实现，出现分歧时覆写。</p>
     *
     * @return UPDATE语句提取器实例
     */
    IUpdateExtractor getUpdateExtractor();

    /**
     * 获取DELETE语句提取器。
     *
     * <p>单表 DELETE 四库语法一致，各方言默认返回通用实现，出现分歧时覆写。</p>
     *
     * @return DELETE语句提取器实例
     */
    IDeleteExtractor getDeleteExtractor();

    /**
     * 获取方言名称标识。
     *
     * @return 方言名称，默认为 "default"
     */
    default String name() { return "default"; }
}
