package com.tbs.cache.caffeine.adapt;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.caffeine.config.property.CaffeineLevelCacheProperty;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Caffeine 缓存装饰器（常规单级场景）
 * <p>
 * 实现 {@link ITtlCacheDecorator}，将 {@link CaffeineCache} 装饰为支持
 * {@code @ExpireOn} 主动过期的 TTL 缓存。与 {@code RedissonTtlCacheDecorator}
 * 对齐：order=1000，先于默认的 {@code DefaultTtlCacheDecorator}
 * （order={@code LOWEST_PRECEDENCE}）匹配。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class CaffeineTtlCacheDecorator implements ITtlCacheDecorator {

    /**
     * Caffeine 配置属性（复用其 {@code expireAfterWrite} 作为默认过期时长）
     */
    @Resource
    private CaffeineLevelCacheProperty caffeineLevelCacheProperty;

    /**
     * 判断当前缓存是否为 Caffeine 缓存
     * <p>
     * 通过 {@link FeatureWrapperHelper} 解包判断，兼容事务感知等包装场景。
     * </p>
     *
     * @param cache   要判断的缓存实例
     * @param manager 缓存管理器
     * @return 是 CaffeineCache 返回 true，否则返回 false
     */
    @Override
    public boolean support(Cache cache, CacheManager manager) {
        return FeatureWrapperHelper.unwrap(cache, CaffeineCache.class).isPresent();
    }

    /**
     * 将 Caffeine 缓存装饰为 TTL 缓存
     *
     * @param cache   要装饰的缓存实例
     * @param manager 缓存管理器
     * @return 装饰后的 {@link CaffeineAdaptTtlCache} 实例
     */
    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        var caffeineCache = FeatureWrapperHelper.unwrap(cache, CaffeineCache.class);
        if (caffeineCache.isPresent()) {
            return new CaffeineAdaptTtlCache(caffeineCache.get(), cache,
                caffeineLevelCacheProperty.getExpireAfterWrite());
        } else {
            AssertUtil.throwError(() -> new IllegalArgumentException("cache is not caffeine cache"));
            return null;
        }
    }

    /**
     * 装饰器顺序
     * <p>
     * 与 {@code RedissonTtlCacheDecorator} 保持一致，先于默认装饰器匹配。
     * </p>
     *
     * @return 顺序值 1000
     */
    @Override
    public int getOrder() {
        return 1000;
    }
}
