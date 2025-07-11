package com.tbs.cache.impl.cache;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.holder.IDictionary;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.HolderUtil;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;

/**
 * 自定义缓存
 *
 * @author abstergo
 */
public class ContextCache extends AbstractValueAdaptingCache {
    String contextId;
    IContext context;

    public ContextCache(boolean allowNullValues, String contextId, IContext cacheContext) {
        super(allowNullValues);
        this.context = cacheContext;
        this.contextId = contextId;
    }

    @Override
    protected Object lookup(Object key) {
        return HolderUtil.get(context, (String)key);
    }

    @Override
    public String getName() {
        return contextId;
    }

    @Override
    public IContext<Object> getNativeCache() {
        return HolderUtil.getContext(contextId);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        String stringKey = (String)key;
        IDictionary<String, Object> source = getNativeCache().getSource();
        if (!source.containsKey(stringKey)) {
            try {
                source.set(stringKey, valueLoader.call());
            } catch (Exception e) {
                throw new FormattedRuntimeException("加载缓存失败", e);
            }
        }
        return (T)source.get(stringKey);
    }

    @Override
    public void put(Object key, Object value) {
        context.getSource().set((String)key, value);
    }

    @Override
    public void evict(Object key) {
        context.getSource().remove((String)key);
    }

    @Override
    public void clear() {
        context.getSource().clear();
    }
}
