package com.tbs.cache.integration;

import com.tbs.cache.ICacheListenner;
import com.tbs.cache.expire.IExpireDetailsManager;
import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.ArrayList;
import java.util.List;
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
     * 过期时间管理器
     */
    private IExpireDetailsManager expireDetailsManager;

    /**
     * 获取过期时间管理器
     *
     * @return
     */
    @Override
    protected IExpireDetailsManager getExpireDetailsManager() {
        if (expireDetailsManager == null) {
            synchronized (this) {
                if (expireDetailsManager == null) {
                    expireDetailsManager = SpringUtil.getBean(IExpireDetailsManager.class);
                }
            }
        }
        return expireDetailsManager;
    }

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
     * 获取原始缓存实例
     *
     * @return 原始Spring Cache实例
     */
    @Override
    public Cache getOriginal() {
        return orginCache;
    }

    /**
     * 设置原始缓存实例
     *
     * @param orginal 新的原始Spring Cache实例
     */
    @Override
    public void setOriginal(Cache orginal) {
        this.orginCache = orginal;
    }

}
