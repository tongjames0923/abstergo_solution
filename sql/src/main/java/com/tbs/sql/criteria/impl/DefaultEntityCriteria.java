package com.tbs.sql.criteria.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.criteria.AbstractEntityCriteria;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.metadata.EntityMetadataExtractor;

/**
 * 默认的基于实体类的查询条件实现类。
 *
 * <p>该类继承自 {@link AbstractEntityCriteria}，提供了基于实体类自动生成查询SQL信息的
 * 默认实现。通过构造函数指定实体类，框架会自动从实体类元数据中提取查询信息。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>基于实体类自动生成查询SQL信息</li>
 *   <li>支持动态设置实体类</li>
 *   <li>提供链式调用的setter方法</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要快速创建基于实体类的查询条件时</li>
 *   <li>需要动态切换实体类时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建查询条件
 * DefaultEntityCriteria criteria = new DefaultEntityCriteria(User.class);
 * 
 * // 动态设置实体类
 * criteria.setEntityClass(Order.class);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实体类不能为null</li>
 *   <li>实体类必须正确配置了表名和字段映射</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see AbstractEntityCriteria
 * @see IQueryCriteria
 */
public class DefaultEntityCriteria extends AbstractEntityCriteria {

    private Class<?> entityClass;

    @Override
    protected SelectInfoModel getSqlSelectInfoModel(Class<?> entity) {
        return EntityMetadataExtractor.pullFromClass(entity);
    }

    public DefaultEntityCriteria(Class<?> entityClass) {
        setEntityClass(entityClass);
    }

    @Override
    protected Class<?> getEntityClass() {
        return entityClass;
    }

    public DefaultEntityCriteria setEntityClass(Class<?> entityClass) {
        AssertUtil.notNull(entityClass, "查询实体类型不可为空。");
        this.entityClass = entityClass;
        return this;
    }
}
