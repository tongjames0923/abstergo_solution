package com.tbs.cache.cascade;

import com.tbs.cache.ICacheListenner;
import com.tbs.cache.integration.AbstractTtlAdaptCacheMananger;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 关联缓存级联失效监听器
 * <p>
 * 框架内置实现，业务侧只需提供 {@link ICacheEvictLink} 声明 Bean，无须在写入点手工逐个清除关联缓存。
 * 触发点挂在删除<b>完成之后</b>的回调上：此时源缓存条目已被真正清除，级联与后续通知看到的都是失效状态。
 * </p>
 * <p>
 * 关键设计约束（改动本类前必读）：
 * <ul>
 *     <li>连带清除一律走 {@link AbstractTtlCache#evictSilently(Object)}，它直达原始缓存、不触发任何回调。
 *         正因如此级联<b>不会递归</b>，运行期不可能成环；代价是<b>级联不传递</b>，多级依赖须在声明里展开，
 *         由 {@link CascadeEvictLinkRegistry} 在装载时告警；</li>
 *     <li>缓存条目自然过期（{@code calledExpire}）不触发级联。派生缓存各有自己的 TTL，
 *     过期属于正常的各自到期，若在过期时连带清除，会把低频的写放大变成高频的读放大；</li>
 *     <li>单条声明执行失败只记录日志并跳过该条，其余声明继续执行，异常不向业务写入路径传播；
 *         此时未清除的派生缓存依赖自身 TTL 达到最终一致。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cache.cascade-evict", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class CascadeEvictCacheListenner implements ICacheListenner {

    @Resource
    private CascadeEvictLinkRegistry registry;

    /**
     * 懒注入，避免监听器在缓存管理器装配完成前被实例化而形成构造期循环
     */
    @Lazy
    @Resource
    private AbstractTtlAdaptCacheMananger cacheManager;

    /**
     * 本监听器按缓存名称匹配声明，不按缓存实例类型筛选
     *
     * @param cache 缓存实例
     * @return 恒为 {@code true}
     */
    @Override
    public boolean support(Cache cache) {
        return true;
    }

    @Override
    public void onGet(String cacheName, Object key, Object value) {
        // 读取不涉及失效，无需处理
    }

    @Override
    public void onPut(String cacheName, Object key, Object value) {
        // 写入不触发级联：只有源数据失效才需要连带失效，回填新值不应抹掉派生缓存
    }

    @Override
    public void onEvict(String cacheName, Object key) {
        // 删除前的回调不用于级联：此刻源缓存条目仍在，级联必须等清除完成
    }

    @Override
    public void onClear(String cacheName) {
        // 清空前的回调不用于级联，处理见 onCleared
    }

    @Override
    public void calledExpire(String cacheName, Object key, java.util.Date expireTime) {
        // 自然过期不触发级联，理由见类注释
    }

    /**
     * 源缓存条目清除完成：展开声明并连带清除目标键
     *
     * @param cacheName 被清除的缓存名称
     * @param key       被清除的缓存键
     */
    @Override
    public void onEvicted(String cacheName, Object key) {
        List<ICacheEvictLink> links = registry.linksOf(unwarpCacheName(cacheName));
        if (CollUtil.isEmpty(links)) {
            return;
        }
        for (ICacheEvictLink link : links) {
            cascadeOne(link, cacheName, key);
        }
    }

    /**
     * 源缓存整体清空完成：连带清空目标缓存
     *
     * @param cacheName 被清空的缓存名称
     */
    @Override
    public void onCleared(String cacheName) {
        List<ICacheEvictLink> links = registry.linksOf(unwarpCacheName(cacheName));
        if (CollUtil.isEmpty(links)) {
            return;
        }
        for (ICacheEvictLink link : links) {
            try {
                clearSilently(link.target(), link.getName());
            } catch (Exception e) {
                log.error("关联失效声明[{}]执行缓存清空失败，源缓存:{}", link.getName(), cacheName, e);
            }
        }
    }

    /**
     * 执行一条声明：反查目标键后逐个静默清除
     * <p>
     * {@link ICacheEvictLink#translate(Object)} 由业务实现、可能查库，异常在此捕获， 一条声明失败不影响其他声明，也不影响已完成的业务写入。
     * </p>
     */
    private void cascadeOne(ICacheEvictLink link, String cacheName, Object key) {
        Collection<Object> targetKeys;
        try {
            targetKeys = link.translate(key);
        } catch (Exception e) {
            log.error("关联失效声明[{}]映射目标键失败，源缓存:{} 源键:{}，本次连带失效跳过", link.getName(), cacheName,
                key, e);
            return;
        }
        if (CollUtil.isEmpty(targetKeys)) {
            log.warn("关联失效清空全部：声明[{}]映射目标键为空，源缓存:{} 源键:{}", link.getName(), cacheName, key);
            clearSilently(link.target(), link.getName());
            return;
        }
        for (Object targetKey : targetKeys) {
            if (targetKey == null) {
                continue;
            }
            try {
                evictSilently(link.target(), targetKey);
                log.debug("关联失效：[{}`,{}] -> [{}`,{}]", cacheName, key, link.target(), targetKey);
            } catch (Exception e) {
                log.error("关联失效声明[{}]清除目标缓存失败，目标缓存:{} 目标键:{}", link.getName(), link.target(),
                    targetKey, e);
            }
        }
    }

    /**
     * 静默清除目标缓存的一个键
     * <p>
     * 目标不是 TTL 缓存实例时退化为直接 {@code evict}：该路径上没有监听挂点，因此不会引起递归。
     * </p>
     */
    private void evictSilently(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("关联失效跳过：目标缓存[{}]未装配", cacheName);
            return;
        }
        Optional<AbstractTtlCache> ttlCache = FeatureWrapperHelper.unwrap(cache, AbstractTtlCache.class);
        if (ttlCache.isPresent()) {
            ttlCache.get().evictSilently(key);
            return;
        }
        cache.evict(key);
    }

    /**
     * 静默清空整个目标缓存
     */
    private void clearSilently(String cacheName, String linkName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("关联失效跳过：目标缓存[{}]未装配，声明:{}", cacheName, linkName);
            return;
        }
        Optional<AbstractTtlCache> ttlCache = FeatureWrapperHelper.unwrap(cache, AbstractTtlCache.class);
        if (ttlCache.isPresent()) {
            ttlCache.get().clearSilently();
            return;
        }
        cache.clear();
    }
}
