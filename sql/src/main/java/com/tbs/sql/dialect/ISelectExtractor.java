package com.tbs.sql.dialect;

import com.tbs.sql.model.select.SelectInfoModel;

/**
 * SELECT语句提取器接口，用于将SELECT信息模型转换为SQL字符串。
 *
 * <p>该接口定义了从 {@link SelectInfoModel} 提取SELECT语句的标准方法。
 * 不同的数据库方言需要实现此接口以提供各自的SELECT语法支持。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从SELECT信息模型中提取SQL字符串</li>
 *   <li>处理数据库特定的SELECT语法差异</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL生成过程中提取SELECT语句</li>
 *   <li>为不同数据库提供SELECT语法支持</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * ISelectExtractor extractor = new MysqlSelectExtractor();
 * String selectSql = extractor.extract(selectInfoModel);
 * // 结果: SELECT * FROM users
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>提取器需要正确处理数据库特定的SELECT语法</li>
 *   <li>需要处理列别名、表别名等情况</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see SelectInfoModel
 * @see com.tbs.sql.dialect.mysql.extractor.MysqlSelectExtractor
 */
public interface ISelectExtractor {
    /**
     * 从SELECT信息模型中提取SELECT语句。
     *
     * @param infoModel SELECT信息模型，包含表名、字段列表等信息
     * @return 提取的SELECT语句字符串
     */
    String extract(SelectInfoModel infoModel);
}
