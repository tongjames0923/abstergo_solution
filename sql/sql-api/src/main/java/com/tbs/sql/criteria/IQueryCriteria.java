package com.tbs.sql.criteria;

import com.tbs.sql.model.select.SelectInfoModel;

import java.io.Serializable;

/**
 * 查询条件接口，定义了查询条件的标准方法。
 *
 * <p>该接口是所有查询条件类的基础接口，定义了获取查询SQL信息的方法。
 * 实现此接口的类可以作为查询条件传递给SQL查询引擎。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取查询SQL信息 {@link #getSelectSQLInfo()}</li>
 *   <li>支持序列化</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>定义查询条件的基类</li>
 *   <li>在SQL查询引擎中传递查询条件</li>
 *   <li>实现自定义查询逻辑</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 实现查询条件接口
 * public class UserQuery implements IQueryCriteria {
 *     @Override
 *     public SelectInfoModel getSelectSQLInfo() {
 *         return SelectInfoModel.rawSql("SELECT * FROM users");
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实现类需要确保返回的SQL信息正确</li>
 *   <li>建议继承 {@link AbstractEntityCriteria} 或 {@link AbstractResourceCriteria}</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see AbstractEntityCriteria
 * @see AbstractResourceCriteria
 */
public interface IQueryCriteria extends Serializable {
    /**
     * 获取查询SQL信息
     *
     * @return SelectInfoModel
     */
    SelectInfoModel getSelectSQLInfo();
}
