package com.tbs.redis.config.property;

import lombok.Data;
import org.redisson.api.EvictionMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("tbs.redission.cache")
public class RedissonCacheProperty {

    /**
     * springboot默认redisCacheing功能的缓存时间 默认-1表示不设置缓存时间，单位秒
     */
    private long cacheTimeout = 3600L;

    /**
     * springboot默认redisCacheing功能的是否接收空值
     */
    private boolean allowNullValues = false;

    /**
     * 是否开启redis事务
     */
    private boolean transaction = false;

    /**
     * redis缓存的key最大空闲时间，超过这个时间就删除，默认30分钟
     */
    private long maxIdleTime = 1800L;

    /**
     * redis缓存的key最大数量，超过这个数量就删除，默认2048
     */
    private int maxSize = 2048;

    /**
     * 缓存的key的清除模式，默认LRU
     */
    private EvictionMode evictionMode = EvictionMode.LRU;



}
