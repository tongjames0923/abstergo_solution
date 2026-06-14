package com.tbs.sql.dialect;

import com.tbs.sql.model.order.OrderInfo;

import java.util.List;

/**
 * ORDER BY语句提取器接口，用于将排序信息列表转换为SQL字符串。
 *
 * <p>该接口定义了从 {@link OrderInfo} 列表提取ORDER BY语句的标准方法。
 * 支持多字段排序，处理升序和降序的语法差异。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从排序信息列表中提取SQL字符串</li>
 *   <li>处理多字段排序</li>
 *   <li>处理数据库特定的排序语法差异</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL生成过程中提取ORDER BY语句</li>
 *   <li>为不同数据库提供排序语法支持</li>
 *   <li>处理多字段组合排序</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * IOrderExtractor extractor = new MysqlOrderExtractor();
 * String orderSql = extractor.extract(orderInfos);
 * // 结果: ORDER BY create_time DESC, update_time ASC
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>提取器需要正确处理升序和降序语法</li>
 *   <li>需要处理空排序列表的情况</li>
 *   <li>需要支持NULL值的排序处理</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see OrderInfo
 * @see com.tbs.sql.dialect.mysql.extractor.MysqlOrderExtractor
 */
public interface IOrderExtractor {
    /**
     * 从排序信息列表中提取ORDER BY语句。
     *
     * @param orderInfos 排序信息列表，包含字段名和排序方向
     * @return 提取的ORDER BY语句字符串，如 "ORDER BY create_time DESC, update_time ASC"，如果列表为空则返回空字符串
     */
    String extract(List<OrderInfo> orderInfos);
}
