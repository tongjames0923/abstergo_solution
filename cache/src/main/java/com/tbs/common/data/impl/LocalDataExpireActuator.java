package com.tbs.common.data.impl;

import com.tbs.cache.core.AbstractScheduleExpiredActuator;
import com.tbs.cache.core.AbstractSpringScheduleExpiredActuator;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;

/**
 * 本地数据过期处理
 *
 * @author tongj
 */
public class LocalDataExpireActuator extends AbstractSpringScheduleExpiredActuator<IDataHolder>
    implements IDataHolderExpireActuator {
    private static class DataHolderRemover implements IExpireRemove<IDataHolder> {
        @Override
        public void remove(IDataHolder cache, Object key) {
            cache.remove(key.toString());
        }
    }

    private DataHolderRemover dataHolderRemover = new DataHolderRemover();

    @Override
    protected IExpireRemove<IDataHolder> getRemoveFunction() {
        return dataHolderRemover;
    }
}
