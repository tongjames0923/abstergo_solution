package com.tbs.sql.dialect;

import com.tbs.sql.model.limit.LimitModel;

/**
 * LIMIT语句提取器接口，用于将分页信息转换为SQL字符串。
 *
 * <p>该接口定义了从 {@link LimitModel} 提取LIMIT语句的标准方法。
 * 支持偏移量和数据量的设置，处理数据库特定的分页语法差异。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从分页信息中提取SQL字符串</li>
 *   <li>处理偏移量和数据量</li>
 *   <li>处理数据库特定的分页语法差异</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL生成过程中提取LIMIT语句</li>
 *   <li>为不同数据库提供分页语法支持</li>
 *   <li>处理大数据量的分页查询</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * ILimitExtractor extractor = new MysqlLimitExtractor();
 * String limitSql = extractor.extract(limitModel);
 * // 结果: LIMIT 0, 10
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>提取器需要正确处理LIMIT语法</li>
 *   <li>需要处理偏移量为0的情况</li>
 *   <li>需要处理数据量为0或负数的情况</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see LimitModel
 * @see com.tbs.sql.dialect.mysql.extractor.MysqlLimitExtractor
 */
public interface ILimitExtractor {
    /**
     * 从分页模型中提取LIMIT语句。
     *
     * @param model 分页模型，包含偏移量和数据量信息
     * @return 提取的LIMIT语句字符串，如 "LIMIT 0, 10"，如果模型为null或无效则返回空字符串
     */
    String extract(LimitModel model);
}
