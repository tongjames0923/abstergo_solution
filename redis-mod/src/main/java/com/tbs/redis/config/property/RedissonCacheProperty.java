package com.tbs.redis.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties("tbs.redission.cache")
@Component
public class RedissonCacheProperty {

    /**
     * springboot默认redisCacheing功能的缓存时间 默认-1表示不设置缓存时间，单位秒
     */
    private long cacheTimeout = -1L;

    /**
     * springboot默认redisCacheing功能的是否接收空值
     */
    private boolean allowNullValues = false;

    /**
     * 是否开启redis事务
     */
    private boolean transaction = false;

    /**
     * redisson配置文件路径
     */
    private String redsictionConfigPath = "";
}
