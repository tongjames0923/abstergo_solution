package com.tbs.shared.interfaces.service;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
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

    private static final int BATCH_UPDATE_SIZE = 30;

    /**
     * 获取禁用的标志
     *
     * @return
     */
    protected abstract Integer getFlagDisable();

    @Override
    protected T handleDelete(K entitys) {
        T disableEntitys = getInstance().queryById(entitys);
        AssertUtil.notNull(disableEntitys, () -> new FormattedRuntimeException("待删除的ID{} 不存在", entitys));
        disableEntitys.setFlag(getFlagDisable());
        getInstance().updateBy(disableEntitys);
        return disableEntitys;
    }

    @Override
    public boolean isDeleted(T entity) {
        return entity != null && Objects.equals(entity.getFlag(), getFlagDisable());
    }
}
