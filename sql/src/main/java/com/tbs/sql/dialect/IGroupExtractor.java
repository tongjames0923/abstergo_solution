package com.tbs.sql.dialect;

import com.tbs.sql.model.group.GroupModel;

import java.util.List;

/**
 * GROUP BY语句提取器接口，用于将分组信息列表转换为SQL字符串。
 *
 * <p>该接口定义了从 {@link GroupModel} 列表提取GROUP BY语句的标准方法。
 * 支持多字段分组，处理数据库特定的分组语法差异。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从分组信息列表中提取SQL字符串</li>
 *   <li>处理多字段分组</li>
 *   <li>处理数据库特定的分组语法差异</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL生成过程中提取GROUP BY语句</li>
 *   <li>为不同数据库提供分组语法支持</li>
 *   <li>处理多字段组合分组</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * IGroupExtractor extractor = new MysqlGroupExtractor();
 * String groupSql = extractor.extract(groupModels);
 * // 结果: GROUP BY status, category
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>提取器需要正确处理GROUP BY语法</li>
 *   <li>需要处理空分组列表的情况</li>
 *   <li>需要配合聚合函数使用</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see GroupModel
 * @see com.tbs.sql.dialect.mysql.extractor.MysqlGroupExtractor
 */
public interface IGroupExtractor {
    /**
     * 从分组信息列表中提取GROUP BY语句。
     *
     * @param groupModels 分组信息列表，包含分组字段名
     * @return 提取的GROUP BY语句字符串，如 "GROUP BY status, category"，如果列表为空则返回空字符串
     */
    String extract(List<GroupModel> groupModels);
}
