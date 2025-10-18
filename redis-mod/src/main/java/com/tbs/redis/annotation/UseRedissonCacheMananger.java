package com.tbs.redis.annotation;

import com.tbs.redis.config.RedissonCacheConfig;
import org.springframework.context.annotation.Import;

@Import(RedissonCacheConfig.class)
public @interface UseRedissonCacheMananger {
}
