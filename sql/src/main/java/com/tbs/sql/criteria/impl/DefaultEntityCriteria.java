package com.tbs.sql.criteria.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.criteria.AbstractEntityCriteria;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.metadata.EntityMetadataExtractor;

/**
 * 默认的基于实体类的查询条件实现
 *
 * @author abstergo
 * @since 1.0.0
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
