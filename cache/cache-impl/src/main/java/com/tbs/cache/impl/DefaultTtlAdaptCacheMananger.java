package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlAdaptCacheMananger;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 默认的TTL缓存管理器实现
 * <p>
 * 继承自AbstractTtlAdaptCacheMananger，实现了TTL缓存管理器的核心功能
 * 负责将普通缓存装饰为支持TTL功能的缓存
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class DefaultTtlAdaptCacheMananger extends AbstractTtlAdaptCacheMananger {

    /**
     * 原始缓存管理器的类类型
     */
    private Class<? extends CacheManager> orginCacheManagerClass;

    /**
     * TTL缓存装饰器集合
     */
    private Collection<ITtlCacheDecorator> ttlCacheDecorators;

    public Collection<ITtlCacheDecorator> getTtlCacheDecorators() {
        if (ttlCacheDecorators == null) {
            synchronized (this) {
                if (ttlCacheDecorators == null) {
                    ttlCacheDecorators = SpringUtil.getBeansOfType(ITtlCacheDecorator.class).values().stream()
                        .sorted(Comparator.comparingInt(ITtlCacheDecorator::getOrder)).collect(Collectors.toList());
                }
            }
        }
        return ttlCacheDecorators;
    }

    /**
     * 原始缓存管理器实例
     */
    private CacheManager orginCacheManager;

    /**
     * 构造方法
     * <p>
     * 创建DefaultTtlAdaptCacheMananger实例
     * </p>
     *
     * @param orginCacheManager 原始缓存管理器的类类型
     */
    public DefaultTtlAdaptCacheMananger(Class<? extends CacheManager> orginCacheManager) {
        this.orginCacheManagerClass = orginCacheManager;
    }

    /**
     * 将普通缓存装饰为TTL缓存
     * <p>
     * 使用ITtlCacheDecorator将普通Cache实例装饰为AbstractTtlCache实例
     * </p>
     *
     * @param cache 要装饰的普通缓存实例
     * @return 装饰后的TTL缓存实例
     */
    @Override
    protected AbstractTtlCache decorateCacheToTtl(Cache cache) {
        if (CollUtil.isNotEmpty(this.getTtlCacheDecorators())) {
            for (ITtlCacheDecorator decorator : this.getTtlCacheDecorators()) {
                if (decorator.support(cache, getOriginal())) {
                    return decorator.decorateCache(cache, getOriginal());
                }
            }
        }
        AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR)
            .message("No TTL cache decorator found for cache: {}", cache.getName()).throwIt();
        return null;
    }

    /**
     * 获取原始缓存管理器实例
     * <p>
     * 懒加载方式获取原始缓存管理器实例
     * 使用双重检查锁定确保线程安全
     * </p>
     *
     * @return 原始缓存管理器实例
     */
    @Override
    public CacheManager getOriginal() {
        if (orginCacheManager == null) {
            synchronized (this) {
                if (orginCacheManager == null) {
                    orginCacheManager = SpringUtil.getBean(orginCacheManagerClass);
                }
            }
        }
        return orginCacheManager;
    }

    /**
     * 设置原始缓存管理器实例
     * <p>
     * 直接设置原始缓存管理器实例，绕过懒加载
     * </p>
     *
     * @param orginal 原始缓存管理器实例
     */
    @Override
    public void setOriginal(CacheManager orginal) {
        this.orginCacheManager = orginal;
    }
}
