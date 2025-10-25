package com.tbs.shared.interfaces.service;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.mapper.IFrameworkMapper;

/**
 * 物理删除全功能服务模板
 *
 * @author tongj
 */
public abstract class AbstractPhysicDeleteFullFeatureServiceTemplate<T extends IIdentifiable<K>, K, M extends IFrameworkMapper<T, K>>
    extends AbstractFullFeatureServiceTemplate<T, K, M> {

    @Override
    protected T handleDelete(K entitys) {
        T deleteEntity = getInstance().queryById(entitys);
        AssertUtil.notNull(deleteEntity, () -> new FormattedRuntimeException("待删除的ID{} 不存在", entitys));
        getMapper().deleteByPrimaryKey(entitys);
        return deleteEntity;
    }

    @Override
    public boolean isDeleted(T entity) {
        return entity == null;
    }
}
