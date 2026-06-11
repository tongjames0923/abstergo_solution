package com.tbs.cache.integration;

import com.tbs.cache.ICacheListenner;
import com.tbs.common.util.framework.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 抽象TTL缓存适配器类，提供基于Spring Cache的TTL（Time To Live）功能实现 该类扩展了AbstractTtlCache，提供了缓存过期机制的通用实现
 * @author abstergo
 */
@Slf4j
public abstract class AbstractAdaptTtlCache extends AbstractTtlCache {
    /**
     * 原始Spring Cache实例
     */
    private Cache orginCache;

    /**
     * 缓存监听器列表
     */
    private List<ICacheListenner> listeners = null;

    /**
     * 全部缓存过期信息的原子引用
     */
    private AtomicReference<ExpireDetails> expireAll = new AtomicReference<>(null);

    /**
     * 单个缓存项过期信息映射表
     */
    private ConcurrentHashMap<Object, ExpireDetails> expireMap = new ConcurrentHashMap<>(32);

    /**
     * 构造函数，初始化原始缓存
     *
     * @param orginCache 原始Spring Cache实例
     */
    public AbstractAdaptTtlCache(Cache orginCache) {
        this.orginCache = orginCache;
    }

    /**
     * 获取缓存监听器列表 通过Spring上下文获取所有实现了ICacheListenner接口的Bean实例 使用双重检查锁定确保线程安全的懒加载
     *
     * @return 缓存监听器列表
     */
    @Override
    protected List<ICacheListenner> getListeners() {
        if (listeners == null) {
            synchronized (this) {
                if (listeners == null) {
                    log.debug("init cache listenner");
                    try {
                        listeners = SpringUtil.getBeansOfType(ICacheListenner.class).values().stream()
                            .collect(Collectors.toList());
                        log.debug("cache listenner init success");
                    } catch (Exception e) {
                        listeners = new ArrayList<>();
                    }
                }
            }
        }
        return listeners;
    }

    /**
     * 标记指定键的缓存项在特定时间过期
     *
     * @param key 缓存项的键
     * @param expireTime 过期时间
     */
    @Override
    public void callToExpire(Object key, Date expireTime) {
        log.debug("call to expire: {} at {}", key, this);
        expireMap.put(key, new ExpireDetails(expireTime, false, key));
    }

    /**
     * 标记所有缓存项在特定时间过期
     *
     * @param expireTime 过期时间
     */
    @Override
    public void callToExpireAll(Date expireTime) {
        log.debug("call to expire all at {}", this);
        expireAll.set(new ExpireDetails(expireTime, true, null));
    }

    /**
     * 检查指定键的缓存项是否被标记为过期
     * 如果已标记过期，则从过期映射中移除并返回过期时间
     *
     * @param key 缓存项的键
     * @return 包含过期时间的Optional对象，如果未过期则为空
     */
    @Override
    protected Optional<Date> isCallToExpire(Object key) {
        return Optional.ofNullable(expireMap.remove(key)).map(expireDetails -> {
            if (expireDetails.isAll()) {
                return null;
            } else {
                return expireDetails.getExpireTime();
            }
        });
    }

    /**
     * 检查是否所有缓存项都被标记为过期
     * 如果已标记过期，则清空过期状态并返回过期时间
     *
     * @return 包含过期时间的Optional对象，如果未过期则为空
     */
    @Override
    protected Optional<Date> isCallToExpireAll() {
        var x = Optional.ofNullable(expireAll.get()).map(expireDetails -> expireDetails.getExpireTime());
        expireAll.set(null);
        return x;
    }

    /**
     * 获取原始缓存实例
     *
     * @return 原始Spring Cache实例
     */
    @Override
    public Cache getOrginal() {
        return orginCache;
    }

    /**
     * 设置原始缓存实例
     *
     * @param orginal 新的原始Spring Cache实例
     */
    @Override
    public void setOrginal(Cache orginal) {
        this.orginCache = orginal;
    }

    /**
     * 缓存过期详细信息内部类
     * 用于存储缓存项的过期时间、是否全部过期以及相关键的信息
     */
    @Data
    @AllArgsConstructor
    private static class ExpireDetails {
        /**
         * 过期时间
         */
        private Date expireTime;

        /**
         * 是否全部缓存项过期
         */
        private boolean isAll;

        /**
         * 相关缓存键
         */
        private Object key;
    }

}
