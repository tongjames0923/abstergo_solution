package com.tbs.sql.criteria;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.model.select.SelectInfoModel;

/**
 * 抽象实体查询条件，基于实体自动产生SelectInfoModel
 *
 * @author abstergo
 * @since 1.0.0
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
