package com.tbs.redis.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@Data
@ConfigurationProperties("tbs.redis")
@Component
public class RedisProperty {

    /**
     * 是否启用redis
     */
    private boolean enable = false;

    /**
     * redis值的序列化器
     */
    private Class<? extends RedisSerializer> ValueSerializer = JdkSerializationRedisSerializer.class;

}
