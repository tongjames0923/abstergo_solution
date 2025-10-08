package com.tbs.cache.core.manager.holder;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IFeatureWrapper;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;

/**
 * 缓存数据源
 *
 * @author tongj
 */
public class DataHolderCache extends AbstractValueAdaptingCache implements IFeatureWrapper<IDataHolder> {
    private IDataHolder dataHolder;

    private String name;

    protected DataHolderCache(boolean allowNullValues, IDataHolder dataHolder, String name) {
        super(allowNullValues);
        this.dataHolder = dataHolder;
        this.name = name;
    }

    @Override
    protected Object lookup(Object key) {
        return dataHolder.get(key.toString()).orElse(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return dataHolder;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        dataHolder.set(key.toString(), value, true);
    }

    @Override
    public void evict(Object key) {
        dataHolder.remove(key.toString());
    }

    @Override
    public void clear() {
        dataHolder.clear();
    }

    @Override
    public IDataHolder getOrginal() {
        return dataHolder;
    }

    @Override
    public void setOrginal(IDataHolder orginal) {
        this.dataHolder = orginal;
    }
}
