package com.tbs.app.expire;

import com.tbs.cache.core.AbstractSpringScheduleExpiredActuator;
import com.tbs.common.data.IDataHolder;

/**
 * 本地缓存过期处理
 *
 * @author tongj
 */
public class LocalDataHolderExpireActuator extends AbstractSpringScheduleExpiredActuator<IDataHolder> {

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
