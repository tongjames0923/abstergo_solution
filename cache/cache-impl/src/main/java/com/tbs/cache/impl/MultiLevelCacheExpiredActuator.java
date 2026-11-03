package com.tbs.cache.impl;

import com.tbs.cache.expire.ICacheExpiredActuator;
import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.Date;
import java.util.List;

/**
 * 多级缓存过期执行器
 * <p>
 * {@link MultiLevelAdaptTtlCache} 的过期落地入口。顶层自己不持有数据，所以本执行器不做过期，只负责把一次过期请求
 * <b>逐层下发</b>给各层级缓存自己的 {@link ICacheExpiredActuator}：每层用什么方式过期由该层的装饰结果决定
 * （典型组合下 L1 走 async 调度式过期，L2 走 Redisson 原生 TTL）。
 * </p>
 * <p>
 * 下发前对每一层调用一次 {@link MultiLevelAdaptTtlCache.IMultiLevelCacheTtlInterceptor#intercept(Cache, int, Object, Date)}
 * 换取本层的最终过期时刻：拦截器返回 {@code null} 视为放行，沿用入参时刻。
 * </p>
 * <p>
 * 「各层落在不同 TTL」只在层级自己支持原生过期时才成立：走原生 TTL 的层级（如 {@code RedissonAdaptTtlCache}）可以被单独
 * 设定过期；而走调度式过期的层级（经 {@code DefaultTtlCacheDecorator} 兜底装饰的 {@code AdaptScheduleExpireTtlCache}，
 * 典型如 Caffeine L1）登记的是到点后按 cacheName 回到 {@code CacheManager} 执行 {@code evict}/{@code clear} 的任务，
 * 多级下该名字解析到的仍是顶层多级缓存，到点会连带其他层一起失效——「只清一层」在这条路径上不成立。
 * </p>
 * <p>
 * 改动本类时务必保持的两条约定：
 * <ol>
 * <li>传给层级执行器的必须是<b>层级缓存实例本身</b>，不能传顶层的多级适配实例——层级执行器靠解包入参获取原生缓存对象
 * （如 {@code RedissonCache}），传错只会留下一条 {@code not support expire} 告警后静默失败，业务表现为缓存不按时过期；</li>
 * <li>传给层级执行器的必须是<b>本层拦截后的过期时刻</b>，而不是原始时刻，否则拦截器等于没配。</li>
 * </ol>
 * </p>
 * <p>
 * 日志统一使用 {@code [MultiLevelTtl]} 前缀，字段顺序固定为
 * {@code cacheName → key → level → cacheClass → interceptedExpireTime → finalExpireTime}：正常下发与逐层成功为 debug，
 * 顶层类型不符、层级未经 TTL 装饰、层级登记失败为 warn。排查「多级缓存 TTL 不生效」时按该前缀检索 warn 即可定位到具体层级；
 * 字段含义与排查口径详见 {@code cache/README.md} 的「多级缓存的逐层过期下发」。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public class MultiLevelCacheExpiredActuator implements ICacheExpiredActuator {

    /**
     * 缓存全部过期处理
     * <p>
     * 全量过期就是 {@code key = null} 的过期请求，与单键过期共用同一条逐层下发链路：下发时对层级执行器调用
     * {@link ICacheExpiredActuator#expireAll(Cache, Date)}（整缓存语义），拦截器同样收到 {@code key = null}，
     * 便于按「整层」维度调整 TTL。绝不把 {@code null} 当作普通缓存键传给层级的按键过期 API。
     * </p>
     *
     * @param cache      多级缓存适配实例，非 {@link MultiLevelAdaptTtlCache} 类型时不处理
     * @param expireTime 期望过期时刻
     * @return 所有层级均登记成功返回 true，任一层级下发失败返回 false
     */
    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        return expire(cache, null, expireTime);
    }

    /**
     * 缓存单键过期处理
     * <p>
     * 逐层流程：取本层拦截后的过期时刻 → 交给本层自己的过期执行器登记 → 汇总各层结果。单层登记失败只告警不上抛、
     * 不重试，其余层级照常下发（尽力而为），失败的层级依赖该层引擎的 TTL 配置达到最终一致。
     * </p>
     *
     * @param cache      多级缓存适配实例，非 {@link MultiLevelAdaptTtlCache} 类型时不处理
     * @param key        待过期的缓存键，{@code null} 表示整层过期
     * @param expireTime 期望过期时刻
     * @return 所有层级均登记成功返回 true，任一层级下发失败返回 false
     */
    @Override
    public boolean expire(Cache cache, Object key, Date expireTime) {
        if (!(cache instanceof MultiLevelAdaptTtlCache)) {
            log.warn("[MultiLevelTtl] cache 不是 MultiLevelAdaptTtlCache 类型，无法逐层下发过期: cacheName={}, key={}",
                cache.getName(), key);
            return false;
        }
        List<Cache> levels = ((MultiLevelAdaptTtlCache)cache).getLevels();
        log.debug("[MultiLevelTtl] 过期下发开始: cacheName={}, key={}, expireTime={}, levelCount={}", cache.getName(),
            key, expireTime, levels.size());

        int i = 0;
        boolean result = true;
        for (Cache level : levels) {
            int levelIndex = ++i;
            Date intercepted = SpringUtil.getBean(MultiLevelAdaptTtlCache.IMultiLevelCacheTtlInterceptor.class)
                .intercept(level, levelIndex, key, expireTime);
            Date finalExpireTime = intercepted == null ? expireTime : intercepted;
            String levelClass = level.getClass().getSimpleName();

            if (!(level instanceof AbstractAdaptTtlCache)) {
                result = false;
                log.warn(
                    "[MultiLevelTtl] 层级未经 TTL 装饰，跳过下发: cacheName={}, key={}, level={}, cacheClass={}, finalExpireTime={}",
                    cache.getName(), key, levelIndex, levelClass, finalExpireTime);
                continue;
            }
            ICacheExpiredActuator levelActuator = ((AbstractAdaptTtlCache)level).getTargetedCacheExpiredActuator();
            boolean levelResult =
                key == null ? levelActuator.expireAll(level, finalExpireTime) : levelActuator.expire(level, key,
                    finalExpireTime);
            result = result && levelResult;
            if (levelResult) {
                log.debug(
                    "[MultiLevelTtl] 层级过期登记成功: cacheName={}, key={}, level={}, cacheClass={}, interceptedExpireTime={}, finalExpireTime={}",
                    cache.getName(), key, levelIndex, levelClass, intercepted, finalExpireTime);
            } else {
                log.warn(
                    "[MultiLevelTtl] 层级过期登记失败，层级执行器未能按该方式过期: cacheName={}, key={}, level={}, cacheClass={}, interceptedExpireTime={}, finalExpireTime={}",
                    cache.getName(), key, levelIndex, levelClass, intercepted, finalExpireTime);
            }
        }

        log.debug("[MultiLevelTtl] 过期下发完成: cacheName={}, key={}, levelCount={}, result={}", cache.getName(), key,
            levels.size(), result);
        return result;
    }
}
