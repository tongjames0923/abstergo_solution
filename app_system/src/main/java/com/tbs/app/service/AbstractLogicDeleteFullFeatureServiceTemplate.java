package com.tbs.app.service;

import com.tbs.app.pojo.entity.IBaseEntity;
import com.tbs.app.pojo.entity.IFlagable;
import com.tbs.app.pojo.entity.IIdentifiable;
import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.common.util.CollUtil;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    protected void handleDelete(List<T> entitys) {
        List<T> disableEntitys = entitys.stream().map(entity -> {
            entity.setFlag(getFlagDisable());
            return entity;
        }).collect(Collectors.toList());
        if (disableEntitys.size() <= BATCH_UPDATE_SIZE) {
            getMapper().batchUpdate(disableEntitys);
        } else {
            for (List<T> batch : CollUtil.partition(disableEntitys, BATCH_UPDATE_SIZE)) {
                getMapper().batchUpdate(batch);
            }
        }
    }
}
