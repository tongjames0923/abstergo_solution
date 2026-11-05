package com.tbs.sql.criteria;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.model.select.SelectInfoModel;

/**
 * 抽象实体查询条件类，基于实体类自动生成查询SQL信息。
 *
 * <p>该类实现了 {@link IQueryCriteria} 接口，提供了基于实体类自动生成查询SQL信息的功能。
 * 子类只需要提供实体类信息，框架会自动从实体类元数据中提取查询信息。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>基于实体类自动生成查询SQL信息</li>
 *   <li>提供实体类信息的抽象方法</li>
 *   <li>验证实体类信息的有效性</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要基于实体类创建查询条件时</li>
 *   <li>需要自动从实体类提取查询信息时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserCriteria extends AbstractEntityCriteria {
 *     @Override
 *     protected Class<?> getEntityClass() {
 *         return User.class;
 *     }
 *
 *     @Override
 *     protected SelectInfoModel getSqlSelectInfoModel(Class<?> entity) {
 *         return EntityMetadataExtractor.pullFromClass(entity);
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>子类必须实现 {@link #getEntityClass()} 方法</li>
 *   <li>子类必须实现 {@link #getSqlSelectInfoModel(Class)} 方法</li>
 *   <li>实体类不能为null</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IQueryCriteria
 * @see DefaultEntityCriteria
 */
public abstract class AbstractEntityCriteria implements IQueryCriteria {

    protected abstract SelectInfoModel getSqlSelectInfoModel(Class<?> entity);

    protected abstract Class<?> getEntityClass();

    @Override
    public SelectInfoModel getSelectSQLInfo() {
        Class<?> entityClass = getEntityClass();
        AssertUtil.notNull(entityClass, "查询实体的类型不可为空。");
        SelectInfoModel selectInfoModel = getSqlSelectInfoModel(entityClass);
        AssertUtil.notNull(selectInfoModel, "获取抽象查询数据为空。");
        return selectInfoModel;
    }
}
