package com.tbs.redis.annotation;

import com.tbs.redis.config.RedissonCacheConfig;
import com.tbs.redis.config.property.RedissonCacheProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableConfigurationProperties({RedissonCacheProperty.class})
@Import({RedissonCacheConfig.class})

public @interface UseRedissonCacheMananger {
}
