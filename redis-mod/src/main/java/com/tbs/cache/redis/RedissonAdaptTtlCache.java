package com.tbs.cache.redis;

import com.tbs.cache.integration.AbstractAdaptTtlCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.time.Duration;

/**
 * Redisson TTL 缓存适配器
 * <p>
 * 包装装饰器入参的原始 Cache 实例，而不是 unwrap 后的 {@code RedissonCache}：外层可能挂着
 * {@code TransactionAwareCacheDecorator}，保留包装才不丢事务同步语义（提交后才写、回滚不写）。
 * 过期动作委托 {@link RedissionCacheExpiredActuator}，由它按传入实例解包出原生 {@code RedissonCache}，
 * 因此解包能力不受影响（{@code FeatureWrapperHelper} 会自动剥离事务包装）。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public class RedissonAdaptTtlCache extends AbstractAdaptTtlCache {

    /**
     * 构造函数，初始化原始缓存与过期执行器
     *
     * @param orginCache            装饰器入参的原始缓存实例（保留外层包装，不在此处 unwrap）
     * @param defaultExpireDuration 整缓存过期未指定过期时刻时的兜底时长，为空取 1 小时
     */
    public RedissonAdaptTtlCache(Cache orginCache, Duration defaultExpireDuration) {
        super(orginCache, new RedissionCacheExpiredActuator(
            defaultExpireDuration != null ? defaultExpireDuration : Duration.ofHours(1)));
    }
}
