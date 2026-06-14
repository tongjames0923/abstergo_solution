package com.tbs.sql.dialect;

import com.tbs.sql.converter.IValueConverter;
import com.tbs.sql.model.where.WhereCompose;

import java.util.List;
import java.util.Map;

/**
 * WHERE语句提取器接口，用于将WHERE条件组合转换为SQL字符串。
 *
 * <p>该接口定义了从 {@link WhereCompose} 提取WHERE语句的标准方法。
 * 支持值转换器的获取，用于处理不同类型的参数值。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从WHERE条件组合中提取SQL字符串</li>
 *   <li>获取值转换器列表 {@link #getValueConverters()}</li>
 *   <li>处理数据库特定的WHERE语法差异</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL生成过程中提取WHERE语句</li>
 *   <li>为不同数据库提供WHERE语法支持</li>
 *   <li>处理参数值的类型转换</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * IWhereExtractor extractor = new MysqlWhereExtractor();
 * String whereSql = extractor.extract(whereCompose, params);
 * // 结果: WHERE status = 1 AND name LIKE '%test%'
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>提取器需要正确处理AND/OR逻辑连接</li>
 *   <li>需要支持子查询和嵌套条件</li>
 *   <li>值转换器需要正确处理不同类型的参数值</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see WhereCompose
 * @see com.tbs.sql.dialect.mysql.extractor.MysqlWhereExtractor
 */
public interface IWhereExtractor {
    /**
     * 从WHERE条件组合中提取WHERE语句。
     *
     * @param whereCompose WHERE条件组合对象
     * @param params SQL参数映射，用于存储参数化查询的参数
     * @return 提取的WHERE语句字符串，如 "WHERE status = 1 AND name LIKE '%test%'"
     */
    String extract(WhereCompose whereCompose, Map<String, Object> params);

    /**
     * 获取值转换器列表，用于处理不同类型的参数值转换。
     *
     * @return 值转换器列表
     */
    List<IValueConverter> getValueConverters();
}
