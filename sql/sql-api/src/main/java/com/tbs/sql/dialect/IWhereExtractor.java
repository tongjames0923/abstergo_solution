package com.tbs.sql.dialect;

import com.tbs.sql.model.where.WhereCompose;

import java.util.Map;

/**
 * WHERE语句提取器接口，用于将WHERE条件组合转换为SQL字符串。
 *
 * <p>该接口定义了从 {@link WhereCompose} 提取WHERE语句的标准方法。
 * 条件值一律以 MyBatis 预处理参数绑定，不进入 SQL 文本。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从WHERE条件组合中提取SQL字符串</li>
 *   <li>把条件值填入参数映射供预处理语句使用</li>
 *   <li>处理数据库特定的WHERE语法差异</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL生成过程中提取WHERE语句</li>
 *   <li>为不同数据库提供WHERE语法支持</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * IWhereExtractor extractor = new CommonWhereExtractor("MySQL");
 * Map<String, Object> params = new HashMap<>();
 * String whereSql = extractor.extract(whereCompose, params);
 * // 结果: WHERE ( (status = #{_prs.param_0}) )
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>提取器需要正确处理AND/OR逻辑连接</li>
 *   <li>需要支持子查询和嵌套条件</li>
 *   <li>条件值不得以字面量拼入返回的SQL片段，只能放进参数映射</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see WhereCompose
 * @see com.tbs.sql.dialect.extractor.CommonWhereExtractor
 */
public interface IWhereExtractor {
    /**
     * 从WHERE条件组合中提取WHERE语句。
     *
     * @param whereCompose WHERE条件组合对象
     * @param params SQL参数映射，用于存储参数化查询的参数
     * @return 提取的WHERE语句字符串，如 "WHERE ( (status = #{_prs.param_0}) )"
     */
    String extract(WhereCompose whereCompose, Map<String, Object> params);
}
