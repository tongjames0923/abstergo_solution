package com.tbs.redis.config.property;

import lombok.Data;
import org.redisson.api.EvictionMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redisson缓存配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.redission.cache前缀的配置属性
 * 用于配置Redisson缓存相关功能
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.redission.cache")
@Component
public class RedissonCacheProperty {

    /**
     * Spring Boot默认RedisCacheing功能的缓存时间
     * <p>
     * 默认值为3600秒（1小时）
     * </p>
     */
    private long cacheTimeout = 3600L;

    /**
     * Spring Boot默认RedisCacheing功能是否接收空值
     * <p>
     * 默认值为false，表示不允许缓存空值
     * </p>
     */
    private boolean allowNullValues = false;

    /**
     * 是否开启Redis事务
     * <p>
     * 默认值为false，表示不开启Redis事务
     * </p>
     */
    private boolean transaction = false;

    /**
     * Redis缓存的key最大空闲时间
     * <p>
     * 超过这个时间的key将被删除，默认值为1800秒（30分钟）
     * </p>
     */
    private long maxIdleTime = 1800L;

    /**
     * Redis缓存的key最大数量
     * <p>
     * 超过这个数量的key将被删除，默认值为2048个
     * </p>
     */
    private int maxSize = 2048;

    /**
     * 缓存的key的清除模式
     * <p>
     * 当达到maxSize限制时，使用此模式清除旧的key，默认值为LRU（最近最少使用）
     * </p>
     */
    private EvictionMode evictionMode = EvictionMode.LRU;



}
