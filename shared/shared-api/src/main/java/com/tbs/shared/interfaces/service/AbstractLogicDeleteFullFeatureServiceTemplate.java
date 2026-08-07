package com.tbs.shared.interfaces.service;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.shared.pojo.IFlagable;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.mapper.IFrameworkMapper;

import java.util.Objects;

/**
 * 逻辑删除的完整功能服务模板
 *
 * @author tongj
 */
public abstract class AbstractLogicDeleteFullFeatureServiceTemplate<T extends IIdentifiable<K> & IFlagable, K, M extends IFrameworkMapper<T, K>>
    extends AbstractFullFeatureServiceTemplate<T, K, M> {

    /**
     * 获取禁用的标志
     *
     * @return
     */
    protected abstract Integer getFlagDeleted();

    @Override
    protected T handleDelete(K entitys) {
        T disableEntitys = getInstance().queryById(entitys);
        AssertUtil.isTrue(disableEntitys == null).i18n("crud.delete.entity.not.exists").params(entitys)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        disableEntitys.setFlag(getFlagDeleted());
        getInstance().updateBy(disableEntitys);
        return disableEntitys;
    }

    @Override
    public boolean isDeleted(T entity) {
        return entity != null && Objects.equals(entity.getFlag(), getFlagDeleted());
    }
}
