package com.tbs.cache.caffeine.adapt;

import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.schedule.api.TaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

import java.time.Duration;
import java.util.Date;

/**
 * Caffeine 缓存的 TTL 适配器（常规单级场景）
 * <p>
 * 继承 {@link AbstractAdaptTtlCache}，将 {@link CaffeineCache} 适配为支持
 * {@code @ExpireOn} 主动过期的 TTL 缓存。
 * </p>
 * <p>
 * 与 Redisson 不同，Caffeine 引擎本身不支持条目级到期时间设置
 * （仅支持缓存级 {@code expireAfterWrite}），因此采用调度式过期：
 * 通过 {@link TaskScheduler} 在指定时刻触发 {@code evict} / {@code clear}。
 * 缓存级原生 TTL 由 {@code CaffeineCacheManager} 创建时的
 * {@code expireAfterWrite} 配置兜底，两者构成双保险。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class CaffeineAdaptTtlCache extends AbstractAdaptTtlCache {

    /**
     * 默认过期时长
     * <p>
     * 未指定过期时间时的兜底值，默认取配置的全局 {@code expireAfterWrite}。
     * </p>
     */
    private final Duration defaultExpireDuration;

    /**
     * 构造方法
     *
     * @param orginCache            原始 CaffeineCache 实例
     * @param orgin                 委托的 Spring Cache 实例
     * @param defaultExpireDuration 默认过期时长，可为 null（兜底 1 分钟）
     */
    public CaffeineAdaptTtlCache(CaffeineCache orginCache, Cache orgin, Duration defaultExpireDuration) {
        super(orgin);
        this.defaultExpireDuration = defaultExpireDuration == null ? Duration.ofMinutes(1) : defaultExpireDuration;
    }

    /**
     * 获取调度器实例
     * <p>
     * 从 Spring 容器懒加载获取 {@link TaskScheduler} 实例，避免构造期依赖容器就绪。
     * </p>
     *
     * @return 调度器实例
     */
    protected TaskScheduler getSchedule() {
        return SpringUtil.getBean(TaskScheduler.class);
    }

    /**
     * 设置整个缓存的过期时间
     * <p>
     * 通过调度器在指定时间清空整个缓存；过期时间为空时使用默认过期时长。
     * </p>
     *
     * @param cache      要设置过期时间的缓存实例
     * @param expireTime 过期时间，可为 null
     * @return 调度任务成功添加且父类处理成功返回 true，否则返回 false
     */
    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        try {
            getSchedule().schedule(() -> cache.clear(), resolveDelay(expireTime));
            return super.expireAll(cache, expireTime);
        } catch (Exception e) {
            log.error("caffeine expireAll error, cache: {}", cache.getName(), e);
            return false;
        }
    }

    /**
     * 设置单个缓存条目的过期时间
     * <p>
     * 通过调度器在指定时间删除特定键的缓存条目。
     * </p>
     *
     * @param cache      要设置过期时间的缓存实例
     * @param key        要设置过期时间的缓存键
     * @param expireTime 过期时间，可为 null
     * @return 调度任务成功添加且父类处理成功返回 true，否则返回 false
     */
    @Override
    public boolean expire(Cache cache, Object key, Date expireTime) {
        try {
            getSchedule().schedule(() -> cache.evict(key), resolveDelay(expireTime));
            return super.expire(cache, key, expireTime);
        } catch (Exception e) {
            log.error("caffeine expire error, cache: {}, key: {}", cache.getName(), key, e);
            return false;
        }
    }

    /**
     * 计算距离过期的延迟时长
     * <p>
     * 过期时间为空时使用默认过期时长；已过期（负值）时收敛为零，立即执行。
     * </p>
     *
     * @param expireTime 过期时间，可为 null
     * @return 延迟时长（非负）
     */
    private Duration resolveDelay(Date expireTime) {
        if (expireTime == null) {
            return defaultExpireDuration;
        }
        Duration delay = Duration.between(new Date().toInstant(), expireTime.toInstant());
        return delay.isNegative() ? Duration.ZERO : delay;
    }
}
