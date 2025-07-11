package com.tbs.cache.impl.manager;

import com.tbs.cache.impl.cache.ContextCache;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.AssertUtil;
import com.tbs.common.util.HolderUtil;
import com.tbs.common.util.StrUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * 缓存管理器
 *
 * @author abstergo
 */
public abstract class AbstractContextCacheManager implements CacheManager {
    private String cacheNameContextId;
    private String cacheContextId;
    private IContext<String> cacheNameContext;
    private IContext<Cache> cacheContext;

    private boolean acceptNull;

    @PostConstruct
    void init() {
        if (StrUtil.isEmpty(cacheNameContextId)) {
            cacheNameContextId = HolderUtil.getDefaultLocalStoredContext().getContextId();
        }
        if (StrUtil.isEmpty(cacheContextId)) {
            cacheContextId = HolderUtil.getDefaultLocalStoredContext().getContextId();
        }
        cacheNameContext = HolderUtil.getContext(cacheNameContextId);
        cacheContext = HolderUtil.getContext(cacheContextId);
        AssertUtil.notNull(cacheNameContext, "cacheNameContext is null");
        AssertUtil.notNull(cacheContext, "cacheContext is null");
    }

    /**
     * 选择缓存名称,用于设置ContextCache的缓存名称同时涉及Context的使用
     *
     * @param name
     * @return
     */
    protected abstract String selectCacheName(String name);

    public AbstractContextCacheManager(String cacheNameContextId, String cacheContextId, boolean acceptNull) {
        this.cacheNameContextId = cacheNameContextId;
        this.cacheContextId = cacheContextId;
        this.acceptNull = acceptNull;
    }

    protected IContext<String> getCacheNameContext() {
        return cacheNameContext;
    }

    protected IContext<Cache> getCacheContext() {
        return cacheContext;
    }

    @Override
    public Cache getCache(String name) {
        String finalName = cacheNameContext.getSource().computeIfAbsent(name, (n) -> {
            return selectCacheName(n);
        });
        return getCacheContext().getSource().computeIfAbsent(finalName, (n) -> {
            return new ContextCache(acceptNull, n, getCacheContext());
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheNameContext.getSource().keys();
    }
}
