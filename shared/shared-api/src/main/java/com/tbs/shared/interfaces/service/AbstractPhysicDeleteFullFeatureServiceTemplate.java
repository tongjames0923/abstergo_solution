package com.tbs.shared.interfaces.service;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.shared.constant.SharedI18nConstant;
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
        AssertUtil.isTrue(deleteEntity == null).i18n(SharedI18nConstant.CRUD_DELETE_ENTITY_NOT_EXISTS).params(entitys)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        getMapper().deleteByPrimaryKey(entitys);
        return deleteEntity;
    }

    @Override
    public boolean isDeleted(T entity) {
        return entity == null;
    }
}
