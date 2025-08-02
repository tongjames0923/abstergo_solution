package com.tbs.redis.config.property;

import com.tbs.redis.constants.RedisBeanNameConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@Data
@ConfigurationProperties("tbs.framework.redis")
@Component
public class RedisProperty {
    /**
     * redis键的序列化器
     */
    private Class<? extends RedisSerializer> KeySerializer = StringRedisSerializer.class;
    /**
     * redis值的序列化器
     */
    private Class<? extends RedisSerializer> ValueSerializer = JdkSerializationRedisSerializer.class;
    /**
     * redis哈希键的序列化器
     */
    private Class<? extends RedisSerializer> HashKeySerializer = StringRedisSerializer.class;
    /**
     * redis哈希值的序列化器
     */
    private Class<? extends RedisSerializer> HashValueSerializer = JdkSerializationRedisSerializer.class;
    /**
     * springboot默认redisCacheing功能的缓存时间
     */
    private long cacheTimeout = 3000;

    /**
     * springboot默认redisCacheing功能的是否接收空值
     */
    private boolean allowNullValues = false;

}
